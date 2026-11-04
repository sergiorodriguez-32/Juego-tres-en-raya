package tresenraya.servidor;

// Clases de Java para leer y escribir texto a través del socket
import java.io.BufferedReader;    // lee texto línea a línea
import java.io.IOException;       // error de entrada/salida (por ejemplo, se cae la conexión)
import java.io.InputStreamReader; // convierte los bytes que llegan en caracteres
import java.io.PrintWriter;       // escribe texto línea a línea
import java.net.Socket;           // la conexión TCP con un cliente

import static tresenraya.comun.MotivoRechazo.COMANDO_DESCONOCIDO;
import static tresenraya.comun.MotivoRechazo.DEBES_EMPEZAR_CON_PLAY;
import static tresenraya.comun.Protocolo.*;

/**
 * CLASE MANEJADORJUGADOR
 * ----------------------
 * Atiende a UN cliente. El servidor crea un objeto de esta clase por cada
 * cliente que se conecta y lo ejecuta en su propio hilo (por eso implementa
 * Runnable). Así el servidor puede atender a varios clientes a la vez.
 *
 * Es el "traductor" entre la red y la Partida. Su trabajo es:
 *  1. Leer los mensajes que envía el cliente (PLAY, MOVE, REMATCH, BYE).
 *  2. Pasárselos a la Partida, que es la que decide qué hacer.
 *  3. Enviar al cliente los mensajes que le mande la Partida (con enviar()).
 *  4. Cuando el cliente se va, avisar a la Partida y cerrar la conexión.
 *
 * Esta clase NO decide nada del juego (turnos, ganador...): eso es de Partida.
 */
public class ManejadorJugador implements Runnable {

    private final Socket socket;   // conexión con este cliente
    private final Partida partida; // la partida compartida (la misma para todos)
    private PrintWriter salida;    // para enviar mensajes a este cliente
    private String nombre;         // nombre que envía el jugador con PLAY
    private char marca;            // 'X' u 'O', la asigna la Partida

    // Pasa a true cuando el jugador ha entrado en la partida con PLAY
    private boolean dentroDeLaPartida = false;

    /** Constructor: recibe el socket del cliente y la partida compartida. */
    public ManejadorJugador(Socket socket, Partida partida) {
        this.socket = socket;
        this.partida = partida;
    }

    /**
     * Código que ejecuta el hilo. Se llama automáticamente al hacer
     * new Thread(manejador).start() en el Servidor.
     */
    @Override
    public void run() {
        // try-with-resources: todo lo que se abre entre los paréntesis se
        // CIERRA AUTOMÁTICAMENTE al salir del try, tanto si termina bien como
        // si hay un error. Así nunca dejamos sockets abiertos (punto de la
        // rúbrica "cierre correcto de recursos").
        try (Socket s = socket;
             // entrada: para leer lo que nos envía el cliente
             BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
             // salida: para enviar al cliente. El "true" es autoflush: cada
             // println se envía al momento, sin quedarse en un búfer.
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            this.salida = out;
            String linea;

            // Bucle principal: leemos mensajes hasta que el cliente se desconecte.
            // readLine() se queda esperando (bloqueado) hasta que llega una línea.
            // Devuelve null cuando el cliente ha cerrado la conexión.
            while ((linea = entrada.readLine()) != null) {
                linea = linea.trim(); // quitamos espacios al principio y al final
                if (linea.isEmpty()) {
                    continue; // ignoramos líneas vacías
                }
                log("<- " + linea); // mostramos en la consola del servidor lo que llega

                // Separamos el comando del resto. split(" ", 2) parte solo en el
                // primer espacio: "MOVE 2,3" -> ["MOVE", "2,3"]
                String[] partes = linea.split(SEPARADOR, 2);
                String comando = partes[0].toUpperCase();                // aceptamos "move" o "MOVE"
                String argumento = (partes.length > 1) ? partes[1] : ""; // lo de después

                // procesar() devuelve false cuando hay que cortar la conexión
                if (!procesar(comando, argumento)) {
                    return; // salimos de run(): el try cierra la conexión y el finally avisa
                }
            }
        } catch (IOException e) {
            // Salta si se cae la conexión de golpe (cierran la ventana, se va la red...)
            log("conexion perdida: " + e.getMessage());
        } finally {
            // El finally se ejecuta SIEMPRE al salir del try, sea por BYE, por
            // desconexión o por error. Aquí avisamos a la partida de que este
            // jugador se ha ido (para dar la victoria por abandono al rival).
            if (dentroDeLaPartida) {
                partida.salir(this);
            }
            log("desconectado");
        }
    }

    /**
     * Decide qué hacer con un comando recibido.
     * Devuelve true para seguir leyendo mensajes, o false para cerrar la
     * conexión (cuando la partida está llena o el cliente envía BYE).
     */
    private boolean procesar(String comando, String argumento) {

        // ---- Antes de entrar en la partida, solo se acepta PLAY <nombre> ----
        if (!dentroDeLaPartida) {
            if (!comando.equals(PLAY) || argumento.isBlank()) {
                enviar(mensaje(DENIED, DEBES_EMPEZAR_CON_PLAY));
                return true;
            }
            nombre = argumento.trim();
            // La Partida decide si entra como X, como O o si está llena
            if (!partida.unirse(this)) {
                enviar(FULL);
                return false; // tercer jugador: cerramos su conexión
            }
            dentroDeLaPartida = true;
            return true;
        }

        // ---- Ya está en la partida: cada comando se lo pasamos a la Partida ----
        switch (comando) {
            case MOVE -> partida.mover(this, argumento);  // jugada
            case REMATCH -> partida.pedirRevancha(this);  // revancha
            case BYE -> {
                return false; // desconexión voluntaria: el finally avisa a la partida
            }
            default -> enviar(mensaje(DENIED, COMANDO_DESCONOCIDO)); // cualquier otra cosa
        }
        return true;
    }

    /**
     * Envía un mensaje a ESTE cliente.
     * Es synchronized para que, si dos hilos quieren escribirle a la vez
     * (por ejemplo, su propio hilo y el del rival a través de la Partida),
     * los mensajes no se mezclen en el socket: primero uno y luego el otro.
     */
    public synchronized void enviar(String mensaje) {
        if (salida != null) {
            salida.println(mensaje); // println añade el salto de línea que espera readLine()
            log("-> " + mensaje);
        }
    }

    // ---- Getters y setters que usa la clase Partida ----

    public String getNombre() {
        return nombre;
    }

    public char getMarca() {
        return marca;
    }

    public void setMarca(char marca) {
        this.marca = marca;
    }

    /**
     * Escribe en la consola del SERVIDOR lo que pasa con este jugador.
     * Muy útil para depurar y para enseñarlo en la demo.
     * Ejemplo: "[Sergio] <- MOVE 2,3"  (<- = recibido, -> = enviado)
     */
    private void log(String texto) {
        // Si aún no sabemos su nombre (antes de PLAY), mostramos su IP y puerto
        String quien = (nombre != null) ? nombre : socket.getRemoteSocketAddress().toString();
        System.out.println("[" + quien + "] " + texto);
    }
}
