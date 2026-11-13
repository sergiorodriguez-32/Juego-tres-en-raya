package tresenraya.cliente;

import java.io.BufferedReader;    // leer texto del servidor línea a línea
import java.io.IOException;       // error de red
import java.io.InputStreamReader; // convierte bytes en caracteres
import java.io.PrintWriter;       // enviar texto al servidor
import java.net.InetSocketAddress; // dirección (IP y puerto) del servidor
import java.net.Socket;           // conexión TCP con el servidor

import static tresenraya.comun.Protocolo.*;

/**
 * CLASE CONEXION
 * --------------
 * Todo lo que el cliente necesita para hablar con el servidor, en un solo
 * objeto: el socket, por dónde se envía, por dónde se recibe y el hilo lector.
 *
 * La usan los DOS clientes (consola y JavaFX), así el código de red está
 * escrito una sola vez. Cada cliente solo se ocupa de su pantalla y de lo que
 * hace el jugador (teclado o clics).
 *
 * Implementa AutoCloseable para poder usarla en un try-with-resources:
 * al salir del try se llama sola a close() y se cierra el socket.
 *
 * Uso:
 *   try (Conexion c = new Conexion(host, puerto, vista)) {
 *       c.jugar("Sergio");   // envía PLAY y arranca el hilo lector
 *       c.mover(2, 3);       // envía MOVE 2,3
 *       c.salir();           // envía BYE
 *   }
 */
public class Conexion implements AutoCloseable {

    /** Tiempo máximo para conectar con el servidor, en milisegundos. */
    private static final int TIEMPO_MAXIMO_MS = 5000;

    private final Socket socket;
    private final BufferedReader entrada;   // lo que llega del servidor
    private final PrintWriter salida;       // lo que enviamos al servidor
    private final EscuchaServidor escucha;  // código del hilo lector

    /**
     * Abre la conexión TCP con el servidor.
     * Lanza IOException si no se puede conectar: servidor apagado, IP
     * equivocada, firewall...
     */
    public Conexion(String host, int puerto, Vista vista) throws IOException {
        socket = new Socket();
        try {
            // Aquí se hace la conexión TCP. Con tiempo máximo: si la IP no existe,
            // falla a los 5 segundos en vez de esperar lo que tarde el sistema.
            socket.connect(new InetSocketAddress(host, puerto), TIEMPO_MAXIMO_MS);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true); // true = autoflush
        } catch (IOException e) {
            socket.close(); // si falla a medias, no dejamos el socket abierto
            throw e;
        }
        escucha = new EscuchaServidor(entrada, vista);
    }

    /**
     * Arranca el hilo lector y pide entrar en la partida con "PLAY <nombre>".
     * Primero el hilo y luego el mensaje, para no perdernos la respuesta.
     */
    public void jugar(String nombre) {
        Thread lector = new Thread(escucha);
        // Hilo "daemon": no impide que el programa termine cuando acaba el main
        lector.setDaemon(true);
        lector.start();
        enviar(mensaje(PLAY, nombre));
    }

    /** Envía "MOVE fila,col". Si no es válida, el servidor responderá DENIED. */
    public void mover(int fila, int col) {
        enviar(mensaje(MOVE, fila + "," + col));
    }

    /** Envía "REMATCH". */
    public void pedirRevancha() {
        enviar(REMATCH);
    }

    /**
     * Envía "BYE". Antes avisa al hilo lector de que somos NOSOTROS los que
     * salimos, para que no lo trate como un corte del servidor.
     */
    public void salir() {
        escucha.avisarDeSalida();
        enviar(BYE);
    }

    /**
     * Envía una línea al servidor.
     * Es synchronized porque en JavaFX pueden enviar dos hilos distintos
     * (el de la ventana y el que cierra la aplicación), y así no se mezclan.
     */
    private synchronized void enviar(String mensaje) {
        salida.println(mensaje);
    }

    /** Cierra el socket. Al cerrarlo, el hilo lector termina solo. */
    @Override
    public void close() throws IOException {
        socket.close();
    }
}
