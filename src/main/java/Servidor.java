import java.io.IOException;   // error de red
import java.net.ServerSocket; // socket del servidor: espera conexiones en un puerto
import java.net.Socket;       // conexión con un cliente concreto

/**
 * CLASE SERVIDOR
 * --------------
 * Programa principal del servidor. Se ejecuta con: java Servidor [puerto]
 * Si no se indica puerto, usa el 5050 (lo pide el enunciado).
 *
 * Funcionamiento:
 * 1. Abre un ServerSocket en el puerto y se queda escuchando.
 * 2. Cada vez que un cliente se conecta, accept() devuelve un Socket.
 * 3. Crea un ManejadorJugador para ese cliente y lo lanza en un HILO NUEVO,
 *    y vuelve a esperar al siguiente cliente.
 *
 * El servidor NO decide si el cliente entra o no: eso lo hace la Partida
 * cuando el cliente envía PLAY (entra como X, como O o recibe FULL).
 * Por eso aceptamos todas las conexiones y es la Partida la que rechaza
 * al tercero.
 */
public class Servidor {

    // Puerto por defecto que indica el enunciado
    public static final int PUERTO_POR_DEFECTO = 5050;

    public static void main(String[] args) {
        // ---- 1. Leer el puerto de los argumentos (si lo hay) ----
        int puerto = PUERTO_POR_DEFECTO;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Puerto no valido, uso el " + PUERTO_POR_DEFECTO);
            }
        }

        // ---- 2. Crear la partida ----
        // Hay UNA sola partida y se la pasamos a todos los manejadores,
        // así todos los hilos trabajan sobre el mismo objeto.
        Partida partida = new Partida();

        // ---- 3. Abrir el puerto y aceptar clientes en bucle ----
        // try-with-resources: el ServerSocket se cierra solo si hay un error
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor del tres en raya escuchando en el puerto " + puerto);

            while (true) { // el servidor no termina nunca; se para con Ctrl+C o el botón Stop
                // accept() se queda esperando hasta que alguien se conecta
                Socket cliente = servidor.accept();
                System.out.println("Nueva conexion desde " + cliente.getRemoteSocketAddress());

                // Un hilo por cliente: así el servidor puede seguir aceptando
                // conexiones y atender a los dos jugadores a la vez.
                new Thread(new ManejadorJugador(cliente, partida)).start();
            }
        } catch (IOException e) {
            // Por ejemplo, si el puerto ya está en uso por otro programa
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}