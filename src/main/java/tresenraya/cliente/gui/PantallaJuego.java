package tresenraya.cliente.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import tresenraya.cliente.Conexion;
import tresenraya.cliente.Vista;
import tresenraya.comun.MotivoRechazo;
import tresenraya.juego.Tablero;

/**
 * CLASE PANTALLAJUEGO
 * -------------------
 * La pantalla de la partida: un tablero de 3x3 botones, un texto que dice
 * qué está pasando y los botones "Revancha" y "Salir".
 *
 *   +---------------------------+
 *   |  Juegas con X             |   <- marca
 *   |  Te toca                  |   <- estado
 *   |   [ X ][   ][ O ]         |
 *   |   [   ][ X ][   ]         |   <- tablero (9 botones)
 *   |   [   ][   ][ O ]         |
 *   |  No permitido: ...        |   <- avisos del servidor
 *   |  [Revancha]  [Salir]      |
 *   +---------------------------+
 *
 * Hace dos cosas:
 *  1. Lo que hace el jugador -> Conexion. Al pulsar una casilla se envía
 *     MOVE fila,col. Aquí NO se comprueba si es nuestro turno ni si la
 *     casilla está libre: lo decide el servidor y, si no vale, responde DENIED.
 *  2. Lo que dice el servidor -> pantalla. Implementa Vista, así que
 *     EscuchaServidor la usa exactamente igual que a VistaConsola.
 *
 * ¿POR QUÉ Platform.runLater EN TODOS LOS MÉTODOS DE VISTA?
 * Los métodos de Vista los llama el hilo LECTOR (EscuchaServidor). JavaFX solo
 * deja modificar la ventana desde su propio hilo (JavaFX Application Thread);
 * si se hace desde otro, lanza una excepción o la ventana se pinta mal.
 * Platform.runLater(tarea) mete la tarea en la cola del hilo de JavaFX, que la
 * ejecuta en cuanto puede. Es el mismo problema de concurrencia que resuelve
 * synchronized en el servidor: un recurso compartido (la ventana) y varios hilos.
 */
public class PantallaJuego implements Vista {

    // Tamaño de la ventana (lo usa también PantallaConexion)
    public static final int ANCHO = 380;
    public static final int ALTO = 500;

    // Colores de cada marca, para distinguirlas de un vistazo
    private static final String COLOR_X = "#2471a3";
    private static final String COLOR_O = "#c0392b";

    private final Scene escena;
    private final Label etiquetaMarca = new Label(); // se rellena con START
    private final Label etiquetaEstado = new Label();
    private final Label etiquetaAviso = new Label();
    private final Button botonRevancha = new Button("Revancha");
    private final Button botonSalir = new Button("Salir");

    // Las 9 casillas. casillas[0][0] es la fila 1, columna 1.
    private final Button[][] casillas = new Button[Tablero.LADO][Tablero.LADO];

    // Nombre de este jugador, para saber si el ganador somos nosotros
    private final String nombre;

    // Se asigna cuando ya estamos conectados (ver AplicacionGui.conectar)
    private Conexion conexion;

    // true mientras en pantalla se ve el resultado de la última partida.
    // Sirve para que un WAIT posterior (revancha o abandono) no lo tape.
    // Solo se usa dentro de runLater, es decir, siempre desde el hilo de JavaFX.
    private boolean resultadoEnPantalla = false;

    public PantallaJuego(String nombre) {
        this.nombre = nombre;

        // ---- Parte de arriba: marca y estado ----
        etiquetaMarca.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        etiquetaEstado.setStyle("-fx-font-size: 18px;");
        VBox arriba = new VBox(5, new Label("Jugador: " + nombre), etiquetaMarca, etiquetaEstado);
        arriba.setAlignment(Pos.CENTER);

        // ---- Centro: el tablero, una rejilla de 3x3 botones ----
        GridPane tablero = new GridPane();
        tablero.setHgap(6);
        tablero.setVgap(6);
        tablero.setAlignment(Pos.CENTER);
        for (int f = 0; f < Tablero.LADO; f++) {
            for (int c = 0; c < Tablero.LADO; c++) {
                Button casilla = new Button();
                casilla.setPrefSize(90, 90);
                casilla.setStyle(estiloCasilla("#000000"));

                // El protocolo usa filas y columnas del 1 al 3, por eso sumamos 1.
                // Las guardamos en variables "final" para poder usarlas en la lambda.
                final int fila = f + 1;
                final int col = c + 1;
                casilla.setOnAction(evento -> conexion.mover(fila, col));

                casillas[f][c] = casilla;
                tablero.add(casilla, c, f); // OJO: GridPane pide (columna, fila)
            }
        }

        // ---- Parte de abajo: avisos y botones ----
        etiquetaAviso.setWrapText(true);
        etiquetaAviso.setMinHeight(40);
        etiquetaAviso.setAlignment(Pos.CENTER);

        botonRevancha.setOnAction(evento -> conexion.pedirRevancha());
        // Platform.exit() cierra la aplicación; JavaFX llamará a AplicacionGui.stop(),
        // que es quien envía BYE y cierra el socket.
        botonSalir.setOnAction(evento -> Platform.exit());

        HBox botones = new HBox(10, botonRevancha, botonSalir);
        botones.setAlignment(Pos.CENTER);
        VBox abajo = new VBox(8, etiquetaAviso, botones);
        abajo.setAlignment(Pos.CENTER);

        // ---- Todo junto: BorderPane coloca cada parte arriba, en el centro y abajo ----
        BorderPane raiz = new BorderPane(tablero, arriba, null, abajo, null);
        raiz.setPadding(new Insets(15));
        BorderPane.setMargin(tablero, new Insets(15, 0, 15, 0));
        escena = new Scene(raiz, ANCHO, ALTO);

        mostrarEstado("Conectando con el servidor...", "#333333");
    }

    public Scene getEscena() {
        return escena;
    }

    public void setConexion(Conexion conexion) {
        this.conexion = conexion;
    }

    // =================================================================
    // Métodos de la interfaz Vista. Los llama el hilo LECTOR, por eso todo
    // lo que tocan de la ventana va dentro de Platform.runLater(...).
    // =================================================================

    @Override
    public void mostrarEsperando() {
        Platform.runLater(() -> {
            if (resultadoEnPantalla) {
                // Tras una revancha o un abandono dejamos el resultado arriba
                // y contamos abajo que estamos esperando
                mostrarAviso("Esperando al otro jugador...", "#333333");
            } else {
                mostrarEstado("Esperando al otro jugador...", "#333333");
                etiquetaAviso.setText("");
            }
        });
    }

    @Override
    public void mostrarPartidaLlena() {
        Platform.runLater(() -> {
            mostrarEstado("La partida esta llena", COLOR_O);
            mostrarAviso("Ya hay dos jugadores. Intentalo mas tarde.", "#333333");
        });
    }

    @Override
    public void mostrarInicio(char miMarca) {
        Platform.runLater(() -> {
            etiquetaMarca.setText("Juegas con " + miMarca);
            etiquetaMarca.setTextFill(Color.web(colorDe(miMarca)));
            etiquetaAviso.setText("");
            resultadoEnPantalla = false; // empieza una partida nueva
            pintarTablero(new Tablero().toString()); // tablero vacío
        });
    }

    @Override
    public void mostrarTablero(String tablero) {
        Platform.runLater(() -> {
            etiquetaAviso.setText("");
            pintarTablero(tablero);
        });
    }

    @Override
    public void mostrarTurno(boolean meToca) {
        Platform.runLater(() -> {
            if (meToca) {
                mostrarEstado("Te toca: pulsa una casilla", "#1e8449");
            } else {
                mostrarEstado("Turno del rival...", "#333333");
            }
        });
    }

    @Override
    public void mostrarRechazo(String codigo, MotivoRechazo motivo) {
        String texto = (motivo != null) ? motivo.getDescripcion() : codigo;
        Platform.runLater(() -> mostrarAviso("No permitido: " + texto, COLOR_O));
    }

    @Override
    public void mostrarEmpate() {
        Platform.runLater(() -> {
            mostrarEstado("Empate!", "#333333");
            mostrarFinPartida();
        });
    }

    @Override
    public void mostrarGanador(String ganador, boolean porAbandono) {
        Platform.runLater(() -> {
            if (ganador.equals(nombre)) {
                mostrarEstado(porAbandono ? "Has ganado: el rival ha abandonado" : "Has ganado!", "#1e8449");
            } else {
                mostrarEstado("Has perdido. Gana " + ganador, COLOR_O);
            }
            mostrarFinPartida();
        });
    }

    @Override
    public void mostrarDesconocido(String linea) {
        Platform.runLater(() -> mostrarAviso("Servidor: " + linea, "#333333"));
    }

    /** El servidor ha cerrado la conexión: desactivamos todo menos "Salir". */
    @Override
    public void conexionCerrada() {
        Platform.runLater(() -> {
            mostrarEstado("Conexion cerrada por el servidor", COLOR_O);
            for (Button[] filaBotones : casillas) {
                for (Button casilla : filaBotones) {
                    casilla.setDisable(true);
                }
            }
            botonRevancha.setDisable(true);
        });
    }

    // =================================================================
    // Métodos auxiliares. Solo se llaman desde dentro de runLater, es
    // decir, ya en el hilo de JavaFX.
    // =================================================================

    /**
     * Pone en cada botón la letra que le corresponde del texto de 9
     * caracteres ("X...O...."), con su color. Las vacías se quedan en blanco.
     */
    private void pintarTablero(String tablero) {
        for (int f = 0; f < Tablero.LADO; f++) {
            for (int c = 0; c < Tablero.LADO; c++) {
                char marca = tablero.charAt(f * Tablero.LADO + c); // misma cuenta que en Tablero.indice()
                Button casilla = casillas[f][c];
                casilla.setText(marca == Tablero.VACIA ? "" : String.valueOf(marca));
                casilla.setStyle(estiloCasilla(colorDe(marca)));
            }
        }
    }

    private void mostrarEstado(String texto, String color) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.setTextFill(Color.web(color));
    }

    private void mostrarAviso(String texto, String color) {
        etiquetaAviso.setText(texto);
        etiquetaAviso.setTextFill(Color.web(color));
    }

    private void mostrarFinPartida() {
        resultadoEnPantalla = true;
        mostrarAviso("Pulsa Revancha para jugar otra vez o Salir para terminar.", "#333333");
    }

    private static String colorDe(char marca) {
        if (marca == Tablero.X) {
            return COLOR_X;
        }
        if (marca == Tablero.O) {
            return COLOR_O;
        }
        return "#000000";
    }

    /** Estilo CSS de una casilla: letra grande y en negrita del color de la marca. */
    private static String estiloCasilla(String colorTexto) {
        return "-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + colorTexto + ";";
    }
}
