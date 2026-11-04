package tresenraya.cliente.gui;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import tresenraya.cliente.Conexion;
import tresenraya.comun.Protocolo;

/**
 * CLASE APLICACIONGUI
 * -------------------
 * La aplicación JavaFX. Controla la ventana y el paso de una pantalla a otra:
 *
 *   PantallaConexion  --(Conectar)-->  PantallaJuego
 *   (nombre, IP, puerto)               (tablero 3x3, revancha, salir)
 *
 * JavaFX llama a sus métodos en este orden:
 *   start(stage) -> al abrir la aplicación
 *   stop()       -> al cerrarla (botón Salir o la X de la ventana)
 *
 * IMPORTANTE: el hilo de JavaFX
 * Todo lo que toca la ventana se ejecuta en un hilo especial, el "JavaFX
 * Application Thread". Si ese hilo se queda bloqueado (por ejemplo, esperando
 * a que conecte un socket) la ventana se congela. Por eso la conexión se hace
 * en otro hilo y, cuando termina, se vuelve al hilo de JavaFX con
 * Platform.runLater(...).
 */
public class AplicacionGui extends Application {

    private Stage ventana;
    private PantallaConexion pantallaConexion;

    // La conexión con el servidor. "volatile" porque se asigna en el hilo que
    // conecta y se lee en el de JavaFX (en stop()).
    private volatile Conexion conexion;

    @Override
    public void start(Stage ventana) {
        this.ventana = ventana;

        // Si se pasan IP y puerto al arrancar, rellenamos los campos con ellos
        List<String> args = getParameters().getRaw();
        String host = args.size() > 0 ? args.get(0) : Protocolo.HOST_POR_DEFECTO;
        String puerto = args.size() > 1 ? args.get(1) : String.valueOf(Protocolo.PUERTO_POR_DEFECTO);

        // Le pasamos a la pantalla el método al que debe llamar al pulsar "Conectar"
        pantallaConexion = new PantallaConexion(host, puerto, this::conectar);

        ventana.setTitle("Tres en raya");
        ventana.setScene(pantallaConexion.getEscena());
        ventana.setResizable(false);
        ventana.show();
    }

    /**
     * Lo llama PantallaConexion al pulsar "Conectar".
     * Se ejecuta en el hilo de JavaFX, así que la conexión la lanzamos en
     * otro hilo para que la ventana no se congele mientras tanto.
     */
    private void conectar(String nombre, String host, int puerto) {
        pantallaConexion.mostrarConectando();

        // La pantalla de juego se crea ya, en el hilo de JavaFX, porque es la
        // Vista que necesita la Conexion para mostrar lo que llegue del servidor.
        PantallaJuego pantallaJuego = new PantallaJuego(nombre);

        Thread hiloConexion = new Thread(() -> {
            try {
                // Esto puede tardar unos segundos si la IP no responde
                Conexion c = new Conexion(host, puerto, pantallaJuego);
                conexion = c;

                // Ya conectados: volvemos al hilo de JavaFX para cambiar de pantalla
                Platform.runLater(() -> {
                    pantallaJuego.setConexion(c);
                    ventana.setScene(pantallaJuego.getEscena());
                    ventana.setTitle("Tres en raya - " + nombre);
                    c.jugar(nombre); // arranca el hilo lector y envía PLAY
                });
            } catch (IOException e) {
                // No se pudo conectar: lo contamos en la misma pantalla
                Platform.runLater(() -> pantallaConexion.mostrarError(
                        "No se pudo conectar con " + host + ":" + puerto + " (" + e.getMessage() + ")"));
            }
        });
        hiloConexion.setDaemon(true); // si se cierra la ventana, no espera a este hilo
        hiloConexion.start();
    }

    /**
     * JavaFX lo llama al cerrar la aplicación. Avisamos al servidor con BYE y
     * cerramos el socket, así el rival gana por abandono si estábamos jugando.
     */
    @Override
    public void stop() {
        Conexion c = conexion;
        if (c != null) {
            c.salir();
            try {
                c.close();
            } catch (IOException e) {
                // Ya estaba cerrada: no hay nada más que hacer
            }
        }
    }
}
