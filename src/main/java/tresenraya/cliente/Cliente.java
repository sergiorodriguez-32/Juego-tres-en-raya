package tresenraya.cliente;

import java.io.IOException; // error de red
import java.util.Scanner;   // leer lo que escribe el usuario por teclado

import tresenraya.comun.Protocolo;

/**
 * CLASE CLIENTE
 * -------------
 * Programa del jugador en modo TEXTO (consola). Se ejecuta con:
 *     java -cp out tresenraya.cliente.Cliente [ip] [puerto]
 * (o con cliente.bat). Por defecto se conecta a localhost (el mismo PC) y al
 * puerto 5050. La versión con ventana está en el paquete gui.
 *
 * Funcionamiento:
 *  1. Pide el nombre al jugador.
 *  2. Se conecta al servidor (clase Conexion) y envía "PLAY <nombre>".
 *  3. A partir de ahí trabaja con DOS HILOS a la vez:
 *     - Hilo LECTOR (EscuchaServidor): escucha siempre al servidor y le pide a
 *       VistaConsola que muestre lo que llega (tablero, turno, errores, fin).
 *     - Hilo PRINCIPAL (esta clase): lee el teclado y envía las jugadas.
 *
 * ¿POR QUÉ DOS HILOS?
 * Leer del teclado (nextLine) y leer del servidor (readLine) son operaciones
 * que se quedan BLOQUEADAS esperando. Con un solo hilo, mientras esperamos a
 * que el usuario escriba no podríamos ver la jugada del rival, y viceversa.
 */
public class Cliente {

    public static void main(String[] args) {
        VistaConsola vista = new VistaConsola();

        // ---- 1. Leer IP y puerto de los argumentos (o usar los de por defecto) ----
        String host = (args.length > 0) ? args[0] : Protocolo.HOST_POR_DEFECTO;
        int puerto = leerPuerto(args, vista);

        // ---- 2. Pedir el nombre ----
        Scanner teclado = new Scanner(System.in);
        String nombre = vista.pedirNombre(teclado);

        // ---- 3. Conectar con el servidor ----
        // try-with-resources: al salir del try la conexión se cierra sola
        try (Conexion conexion = new Conexion(host, puerto, vista)) {
            vista.mostrarConectado(host, puerto);

            // ---- 4. Arrancar el hilo lector y pedir entrar en la partida ----
            conexion.jugar(nombre);
            vista.mostrarAyuda();

            // ---- 5. Hilo PRINCIPAL: leer el teclado y enviar comandos ----
            leerTeclado(teclado, conexion, vista);

        } catch (IOException e) {
            // Salta si el servidor no está arrancado, la IP es incorrecta o un firewall bloquea
            vista.mostrarErrorConexion(e.getMessage());
        }
        vista.mostrarDespedida();
    }

    /**
     * Bucle del hilo principal: lee lo que escribe el jugador y lo traduce a
     * un mensaje del protocolo. Termina cuando el jugador escribe "salir".
     *
     *   salir     -> BYE
     *   revancha  -> REMATCH
     *   2,3       -> MOVE 2,3
     *   otra cosa -> se muestra la ayuda (no se envía nada)
     */
    private static void leerTeclado(Scanner teclado, Conexion conexion, VistaConsola vista) {
        while (teclado.hasNextLine()) {
            String texto = teclado.nextLine().trim();

            if (texto.equalsIgnoreCase("salir")) {
                conexion.salir();
                return; // al volver al main, el try cierra la conexión

            } else if (texto.equalsIgnoreCase("revancha")) {
                conexion.pedirRevancha();

            } else if (texto.matches("\\d\\s*,\\s*\\d")) {
                // Expresión regular: un dígito, una coma y otro dígito,
                // con espacios opcionales alrededor de la coma. Ej: "2,3" o "2 , 3"
                // OJO: aquí NO comprobamos si es del 1 al 3 ni si es nuestro turno;
                // eso lo valida el servidor y nos responde DENIED si está mal.
                String[] partes = texto.replace(" ", "").split(",");
                conexion.mover(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));

            } else if (!texto.isEmpty()) {
                // Cualquier otra cosa: recordamos los comandos
                vista.mostrarAyuda();
            }
        }
    }

    /**
     * Devuelve el puerto pasado como segundo argumento, o el 5050 si no se
     * pasa ninguno o no es un número.
     */
    private static int leerPuerto(String[] args, VistaConsola vista) {
        if (args.length < 2) {
            return Protocolo.PUERTO_POR_DEFECTO;
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            vista.mostrarPuertoNoValido(Protocolo.PUERTO_POR_DEFECTO);
            return Protocolo.PUERTO_POR_DEFECTO;
        }
    }
}
