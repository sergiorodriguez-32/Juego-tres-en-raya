import java.io.BufferedReader;    // leer texto del servidor línea a línea
import java.io.IOException;       // error de red
import java.io.InputStreamReader; // convierte bytes en caracteres
import java.io.PrintWriter;       // enviar texto al servidor
import java.net.Socket;           // conexión TCP con el servidor
import java.util.Scanner;         // leer lo que escribe el usuario por teclado

/**
 * CLASE CLIENTE
 * -------------
 * Programa del jugador. Se ejecuta con:  java Cliente [ip] [puerto]
 * Por defecto se conecta a localhost (el mismo PC) y al puerto 5050.
 *
 * Funcionamiento:
 *  1. Pide el nombre al jugador.
 *  2. Se conecta al servidor y envía "PLAY <nombre>".
 *  3. A partir de ahí trabaja con DOS HILOS a la vez:
 *     - Hilo LECTOR: escucha siempre al servidor y muestra lo que llega
 *       (tablero, turno, errores, fin de partida).
 *     - Hilo PRINCIPAL: lee el teclado y envía las jugadas.
 *
 * ¿POR QUÉ DOS HILOS?
 * Leer del teclado (nextLine) y leer del servidor (readLine) son operaciones
 * que se quedan BLOQUEADAS esperando. Con un solo hilo, mientras esperamos a
 * que el usuario escriba no podríamos ver la jugada del rival, y viceversa.
 */
public class Cliente {

    // Marca de este jugador ('X' u 'O'). La recibe del servidor con START.
    // "volatile" porque la escribe el hilo lector y la lee el hilo lector y el
    // principal: volatile garantiza que todos los hilos ven el valor actualizado.
    private static volatile char miMarca = '?';

    // Se pone a true cuando el usuario escribe "salir", para no mostrar
    // "Conexion cerrada por el servidor" cuando somos nosotros los que salimos.
    private static volatile boolean saliendo = false;

    public static void main(String[] args) {

        // ---- 1. Leer IP y puerto de los argumentos (o usar los de por defecto) ----
        String host = (args.length > 0) ? args[0] : "localhost";
        int puerto = (args.length > 1) ? Integer.parseInt(args[1]) : 5050;

        // ---- 2. Pedir el nombre ----
        Scanner teclado = new Scanner(System.in);
        System.out.print("Tu nombre: ");
        String nombre = teclado.nextLine().trim();

        // ---- 3. Conectar con el servidor ----
        // try-with-resources: socket, entrada y salida se cierran solos al terminar
        try (Socket socket = new Socket(host, puerto); // aquí se hace la conexión TCP
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) { // true = autoflush

            System.out.println("Conectado a " + host + ":" + puerto);

            // ---- 4. Arrancar el hilo LECTOR ----
            // La lambda () -> escucharServidor(entrada) es el código que ejecutará el hilo.
            Thread lector = new Thread(() -> escucharServidor(entrada));
            // Hilo "daemon": no impide que el programa termine cuando acaba el main
            lector.setDaemon(true);
            lector.start();

            // ---- 5. Pedir entrar en la partida ----
            salida.println("PLAY " + nombre);
            mostrarAyuda();

            // ---- 6. Hilo PRINCIPAL: leer el teclado y enviar comandos ----
            while (teclado.hasNextLine()) {
                String texto = teclado.nextLine().trim();

                if (texto.equalsIgnoreCase("salir")) {
                    // Desconexión voluntaria
                    saliendo = true;
                    salida.println("BYE");
                    break; // salimos del bucle y el try cierra la conexión
                } else if (texto.equalsIgnoreCase("revancha")) {
                    salida.println("REMATCH");
                } else if (texto.matches("\\d\\s*,\\s*\\d")) {
                    // Expresión regular: un dígito, una coma y otro dígito,
                    // con espacios opcionales alrededor de la coma. Ej: "2,3" o "2 , 3"
                    // Quitamos los espacios y enviamos por ejemplo "MOVE 2,3".
                    // OJO: aquí NO comprobamos si es del 1 al 3 ni si es nuestro turno;
                    // eso lo valida el servidor y nos responde DENIED si está mal.
                    salida.println("MOVE " + texto.replace(" ", ""));
                } else if (!texto.isEmpty()) {
                    // Cualquier otra cosa: recordamos los comandos
                    mostrarAyuda();
                }
            }
        } catch (IOException e) {
            // Salta si el servidor no está arrancado, la IP es incorrecta o un firewall bloquea
            System.out.println("No se pudo conectar con el servidor: " + e.getMessage());
        }
        System.out.println("Hasta luego.");
    }

    /**
     * Código del hilo LECTOR: lee del servidor en bucle y procesa cada mensaje.
     * Termina cuando el servidor cierra la conexión (readLine devuelve null).
     */
    private static void escucharServidor(BufferedReader entrada) {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                procesarMensaje(linea);
            }
        } catch (IOException e) {
            // El socket se ha cerrado: no hace falta hacer nada más
        }
        // Si la conexión se cierra sin que el usuario haya escrito "salir",
        // es que la ha cerrado el servidor (por ejemplo, tras FULL): cerramos el programa.
        if (!saliendo) {
            System.out.println("\nConexion cerrada por el servidor.");
            System.exit(0);
        }
    }

    /**
     * Interpreta un mensaje del servidor según el protocolo y lo muestra
     * de forma entendible para el jugador.
     */
    private static void procesarMensaje(String linea) {
        // Partimos el mensaje por espacios. Ej: "END WIN Sergio" -> ["END", "WIN", "Sergio"]
        String[] partes = linea.split(" ");

        switch (partes[0]) {
            // WAIT: estamos esperando a que llegue el rival (o a que acepte la revancha)
            case "WAIT" -> System.out.println("Esperando al otro jugador...");

            // FULL: ya hay dos jugadores; el servidor cerrará la conexión
            case "FULL" -> System.out.println("La partida esta llena. Intentalo mas tarde.");

            // START X / START O: empieza la partida y nos dice nuestra marca
            case "START" -> {
                miMarca = partes[1].charAt(0);
                System.out.println("\nEmpieza la partida! Juegas con " + miMarca);
                dibujarTablero("........."); // tablero vacío
            }

            // OK <tablero>: jugada aceptada; nos llega el tablero actualizado
            case "OK" -> dibujarTablero(partes[1]);

            // TURN X / TURN O: de quién es el turno ahora
            case "TURN" -> {
                if (partes[1].charAt(0) == miMarca) {
                    System.out.println("Te toca. Escribe fila,columna (por ejemplo 2,3):");
                } else {
                    System.out.println("Turno del rival...");
                }
            }

            // DENIED <motivo>: nuestra jugada no es válida (no es nuestro turno, casilla ocupada...)
            case "DENIED" -> System.out.println("Movimiento no valido: " + partes[1]);

            // END WIN <ganador> [ABANDONO] / END DRAW -: fin de la partida
            case "END" -> {
                if (partes[1].equals("DRAW")) {
                    System.out.println("Empate!");
                } else {
                    String ganador = partes[2];
                    // Si hay una cuarta palabra "ABANDONO", el rival se ha ido
                    boolean abandono = partes.length > 3 && partes[3].equals("ABANDONO");
                    System.out.println("Gana " + ganador + (abandono ? " (el rival ha abandonado)" : "") + ".");
                }
                System.out.println("Escribe 'revancha' para jugar otra vez o 'salir' para terminar.");
            }

            // Cualquier otro mensaje que no conozcamos: lo mostramos tal cual
            default -> System.out.println("Servidor: " + linea);
        }
    }

    /**
     * Dibuja el tablero en la consola a partir del texto de 9 caracteres.
     * Ejemplo: "X...O...." se dibuja como
     *
     *      1   2   3
     *   1  X |   |
     *     ---+---+---
     *   2    | O |
     *     ---+---+---
     *   3    |   |
     */
    private static void dibujarTablero(String t) {
        System.out.println();
        System.out.println("     1   2   3"); // números de columna
        for (int fila = 0; fila < 3; fila++) {
            System.out.print("  " + (fila + 1) + " "); // número de fila
            for (int col = 0; col < 3; col++) {
                char c = t.charAt(fila * 3 + col); // posición en el texto de 9 caracteres
                System.out.print(" " + (c == '.' ? ' ' : c) + " "); // el '.' se pinta como hueco
                if (col < 2) {
                    System.out.print("|"); // separador entre columnas
                }
            }
            System.out.println();
            if (fila < 2) {
                System.out.println("    ---+---+---"); // separador entre filas
            }
        }
        System.out.println();
    }

    /** Muestra los comandos que puede escribir el jugador. */
    private static void mostrarAyuda() {
        System.out.println("Comandos: fila,columna (del 1 al 3) para jugar | revancha | salir");
    }
}