package tresenraya.cliente.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * CLASE PANTALLACONEXION
 * ----------------------
 * Primera pantalla: el jugador escribe su nombre, la IP y el puerto del
 * servidor y pulsa "Conectar".
 *
 * Esta clase solo dibuja el formulario y comprueba que los datos tienen
 * sentido. La conexión de verdad la hace AplicacionGui: al pulsar el botón,
 * la pantalla llama a la función que recibió en el constructor (AlConectar).
 */
public class PantallaConexion {

    /**
     * Lo que hay que hacer cuando el jugador pulsa "Conectar".
     * Es una interfaz con un solo método, así que se puede pasar un método
     * (this::conectar) o una lambda.
     */
    @FunctionalInterface
    public interface AlConectar {
        void conectar(String nombre, String host, int puerto);
    }

    private final Scene escena;
    private final TextField campoNombre = new TextField();
    private final TextField campoHost = new TextField();
    private final TextField campoPuerto = new TextField();
    private final Button botonConectar = new Button("Conectar");
    private final Label mensaje = new Label();

    public PantallaConexion(String hostInicial, String puertoInicial, AlConectar alConectar) {
        campoHost.setText(hostInicial);
        campoPuerto.setText(puertoInicial);
        campoNombre.setPromptText("Tu nombre");

        // ---- Formulario: una rejilla de 2 columnas (etiqueta | campo) ----
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("Nombre:"), campoNombre);
        formulario.addRow(1, new Label("IP del servidor:"), campoHost);
        formulario.addRow(2, new Label("Puerto:"), campoPuerto);

        // El formulario va dentro de una tarjeta
        formulario.getStyleClass().add("tarjeta");
        campoNombre.setPrefWidth(200);

        Label titulo = new Label("Tres en raya");
        titulo.getStyleClass().add("titulo");
        Label subtitulo = new Label("Online · 2 jugadores");
        subtitulo.getStyleClass().add("subtitulo");

        Label ayuda = new Label("Si el servidor esta en este mismo PC, deja la IP como localhost.");
        ayuda.getStyleClass().add("ayuda");
        ayuda.setWrapText(true);

        botonConectar.getStyleClass().add("boton-primario");
        botonConectar.setDefaultButton(true); // se pulsa también con Enter
        botonConectar.setMaxWidth(Double.MAX_VALUE);
        mensaje.setWrapText(true);

        // Al pulsar: validamos los campos y, si están bien, avisamos a AplicacionGui
        botonConectar.setOnAction(evento -> {
            String nombre = campoNombre.getText().trim();
            String host = campoHost.getText().trim();
            String textoPuerto = campoPuerto.getText().trim();

            // El nombre no puede tener espacios: el protocolo separa por espacios
            // y "END WIN Ana Maria" se leería mal en el cliente.
            if (nombre.isEmpty() || nombre.contains(" ")) {
                mostrarError("Escribe un nombre sin espacios.");
                return;
            }
            if (host.isEmpty()) {
                mostrarError("Escribe la IP del servidor (o localhost).");
                return;
            }
            int puerto;
            try {
                puerto = Integer.parseInt(textoPuerto);
            } catch (NumberFormatException e) {
                mostrarError("El puerto tiene que ser un numero (por defecto 5050).");
                return;
            }
            alConectar.conectar(nombre, host, puerto);
        });

        // ---- Todo apilado en vertical ----
        VBox cabecera = new VBox(2, titulo, subtitulo);
        cabecera.setAlignment(Pos.CENTER);
        VBox raiz = new VBox(18, cabecera, formulario, ayuda, botonConectar, mensaje);
        raiz.setPadding(new Insets(30));
        raiz.setAlignment(Pos.CENTER_LEFT);
        // Mismo tamaño que PantallaJuego, para que la ventana no cambie al conectar
        escena = new Scene(raiz, PantallaJuego.ANCHO, PantallaJuego.ALTO);
        PantallaJuego.aplicarEstilo(escena);
    }

    public Scene getEscena() {
        return escena;
    }

    /** Mientras se conecta, bloqueamos el botón para no conectar dos veces. */
    public void mostrarConectando() {
        botonConectar.setDisable(true);
        mensaje.setStyle("-fx-text-fill: #cfd8e3;");
        mensaje.setText("Conectando...");
    }

    /** Muestra un error en rojo y vuelve a activar el botón. */
    public void mostrarError(String texto) {
        botonConectar.setDisable(false);
        mensaje.setStyle("-fx-text-fill: #ff6b81;");
        mensaje.setText(texto);
    }
}
