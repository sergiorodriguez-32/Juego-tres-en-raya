package tresenraya.servidor;

import java.io.IOException;   // error de red
import java.net.ServerSocket; // socket del servidor: espera conexiones en un puerto
import java.net.Socket;       // conexión con un cliente concreto

import tresenraya.comun.Protocolo;

/**
 * CLASE SERVIDOR
 * --------------
 * Programa principal del servidor. Se ejecuta con:
 *     java -cp out tresenraya.servidor.Servidor [puerto]
 * (o con servidor.bat). Si no se indica puerto, usa el 5050.
 *
 * Funcionamiento:
 *  1. Abre un ServerSocket en el puerto y se queda escuchando.
 *  2. Cada vez que un cliente se conecta, accept() devuelve un Socket.
 *  3. Crea un ManejadorJugador para ese cliente y lo lanza en un HILO NUEVO,
 *     y vuelve a esperar al siguiente cliente.
 *
 * El servidor NO decide si el cliente entra o no: eso lo hace la Partida
 * cuando el cliente envía PLAY (entra como X, como O o recibe FULL).
 * Por eso aceptamos todas las conexiones y es la Partida la que rechaza
 * al tercero.
 */
public class Servidor {

    public static void main(String[] args) {

        // ---- 1. Leer el puerto de los argumentos (si lo hay) ----
        int puerto = leerPuerto(args);

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

    /**
     * Devuelve el puerto pasado como primer argumento, o el 5050 si no se
     * pasa ninguno o no es un número.
     */
    private static int leerPuerto(String[] args) {
        if (args.length == 0) {
            return Protocolo.PUERTO_POR_DEFECTO;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Puerto no valido, uso el " + Protocolo.PUERTO_POR_DEFECTO);
            return Protocolo.PUERTO_POR_DEFECTO;
        }
    }
}
