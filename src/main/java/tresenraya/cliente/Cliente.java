package tresenraya.cliente;

import java.io.BufferedReader;    // leer texto del servidor línea a línea
import java.io.IOException;       // error de red
import java.io.InputStreamReader; // convierte bytes en caracteres
import java.io.PrintWriter;       // enviar texto al servidor
import java.net.Socket;           // conexión TCP con el servidor
import java.util.Scanner;         // leer lo que escribe el usuario por teclado

import tresenraya.comun.Protocolo;

import static tresenraya.comun.Protocolo.*;

/**
 * CLASE CLIENTE
 * -------------
 * Programa del jugador. Se ejecuta con:
 *     java -cp out tresenraya.cliente.Cliente [ip] [puerto]
 * (o con cliente.bat). Por defecto se conecta a localhost (el mismo PC) y al
 * puerto 5050.
 *
 * Funcionamiento:
 *  1. Pide el nombre al jugador.
 *  2. Se conecta al servidor y envía "PLAY <nombre>".
 *  3. A partir de ahí trabaja con DOS HILOS a la vez:
 *     - Hilo LECTOR (clase EscuchaServidor): escucha siempre al servidor y
 *       muestra lo que llega (tablero, turno, errores, fin de partida).
 *     - Hilo PRINCIPAL (esta clase): lee el teclado y envía las jugadas.
 *
 * ¿POR QUÉ DOS HILOS?
 * Leer del teclado (nextLine) y leer del servidor (readLine) son operaciones
 * que se quedan BLOQUEADAS esperando. Con un solo hilo, mientras esperamos a
 * que el usuario escriba no podríamos ver la jugada del rival, y viceversa.
 *
 * Reparto del cliente en clases:
 *  - Cliente:         conexión + teclado (lo que el jugador ENVÍA)
 *  - EscuchaServidor: lo que el servidor RESPONDE
 *  - VistaConsola:    todo lo que se ESCRIBE en pantalla
 */
public class Cliente {

    public static void main(String[] args) {

        // ---- 1. Leer IP y puerto de los argumentos (o usar los de por defecto) ----
        String host = (args.length > 0) ? args[0] : Protocolo.HOST_POR_DEFECTO;
        int puerto = leerPuerto(args);

        // ---- 2. Pedir el nombre ----
        Scanner teclado = new Scanner(System.in);
        String nombre = VistaConsola.pedirNombre(teclado);

        // ---- 3. Conectar con el servidor ----
        // try-with-resources: socket, entrada y salida se cierran solos al terminar
        try (Socket socket = new Socket(host, puerto); // aquí se hace la conexión TCP
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) { // true = autoflush

            VistaConsola.mostrarConectado(host, puerto);

            // ---- 4. Arrancar el hilo LECTOR ----
            EscuchaServidor escucha = new EscuchaServidor(entrada);
            Thread lector = new Thread(escucha);
            // Hilo "daemon": no impide que el programa termine cuando acaba el main
            lector.setDaemon(true);
            lector.start();

            // ---- 5. Pedir entrar en la partida ----
            salida.println(mensaje(PLAY, nombre));
            VistaConsola.mostrarAyuda();

            // ---- 6. Hilo PRINCIPAL: leer el teclado y enviar comandos ----
            leerTeclado(teclado, salida, escucha);

        } catch (IOException e) {
            // Salta si el servidor no está arrancado, la IP es incorrecta o un firewall bloquea
            VistaConsola.mostrarErrorConexion(e.getMessage());
        }
        VistaConsola.mostrarDespedida();
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
    private static void leerTeclado(Scanner teclado, PrintWriter salida, EscuchaServidor escucha) {
        while (teclado.hasNextLine()) {
            String texto = teclado.nextLine().trim();

            if (texto.equalsIgnoreCase("salir")) {
                // Desconexión voluntaria. Avisamos antes al hilo lector para que
                // no muestre "Conexion cerrada por el servidor".
                escucha.avisarDeSalida();
                salida.println(BYE);
                return; // al volver al main, el try cierra la conexión

            } else if (texto.equalsIgnoreCase("revancha")) {
                salida.println(REMATCH);

            } else if (texto.matches("\\d\\s*,\\s*\\d")) {
                // Expresión regular: un dígito, una coma y otro dígito,
                // con espacios opcionales alrededor de la coma. Ej: "2,3" o "2 , 3"
                // Quitamos los espacios y enviamos por ejemplo "MOVE 2,3".
                // OJO: aquí NO comprobamos si es del 1 al 3 ni si es nuestro turno;
                // eso lo valida el servidor y nos responde DENIED si está mal.
                salida.println(mensaje(MOVE, texto.replace(" ", "")));

            } else if (!texto.isEmpty()) {
                // Cualquier otra cosa: recordamos los comandos
                VistaConsola.mostrarAyuda();
            }
        }
    }

    /**
     * Devuelve el puerto pasado como segundo argumento, o el 5050 si no se
     * pasa ninguno o no es un número.
     */
    private static int leerPuerto(String[] args) {
        if (args.length < 2) {
            return Protocolo.PUERTO_POR_DEFECTO;
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            VistaConsola.mostrarPuertoNoValido(Protocolo.PUERTO_POR_DEFECTO);
            return Protocolo.PUERTO_POR_DEFECTO;
        }
    }
}
