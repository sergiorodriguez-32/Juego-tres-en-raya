package tresenraya.cliente;

import java.util.Scanner;

import tresenraya.comun.MotivoRechazo;
import tresenraya.juego.Tablero;

/**
 * CLASE VISTACONSOLA
 * ------------------
 * TODO lo que el cliente escribe en la pantalla está aquí: el tablero, los
 * avisos de turno, los errores, la ayuda...
 *
 * ¿Por qué separarlo?
 *  - Cliente y EscuchaServidor se ocupan solo de la comunicación; no tienen
 *    ningún System.out.println mezclado con la lógica.
 *  - Si queremos cambiar un texto o el dibujo del tablero, solo hay que
 *    mirar esta clase.
 *  - Si algún día se hace una interfaz gráfica, bastaría con sustituir esta
 *    clase por otra que pinte en una ventana.
 *
 * Todos los métodos son static porque la clase no guarda ningún dato: solo
 * escribe en la consola.
 */
public final class VistaConsola {

    private VistaConsola() {
        // No se instancia: solo tiene métodos static
    }

    // ---- Conexión ----

    public static String pedirNombre(Scanner teclado) {
        System.out.print("Tu nombre: ");
        return teclado.nextLine().trim();
    }

    public static void mostrarConectado(String host, int puerto) {
        System.out.println("Conectado a " + host + ":" + puerto);
    }

    public static void mostrarPuertoNoValido(int puertoPorDefecto) {
        System.out.println("Puerto no valido, uso el " + puertoPorDefecto);
    }

    public static void mostrarErrorConexion(String detalle) {
        System.out.println("No se pudo conectar con el servidor: " + detalle);
    }

    public static void mostrarConexionCerrada() {
        System.out.println("\nConexion cerrada por el servidor.");
    }

    public static void mostrarDespedida() {
        System.out.println("Hasta luego.");
    }

    // ---- Mensajes del servidor ----

    public static void mostrarEsperando() {
        System.out.println("Esperando al otro jugador...");
    }

    public static void mostrarPartidaLlena() {
        System.out.println("La partida esta llena. Intentalo mas tarde.");
    }

    public static void mostrarInicio(char miMarca) {
        System.out.println("\nEmpieza la partida! Juegas con " + miMarca);
        dibujarTablero(new Tablero().toString()); // tablero vacío: "........."
    }

    public static void mostrarTurno(boolean meToca) {
        if (meToca) {
            System.out.println("Te toca. Escribe fila,columna (por ejemplo 2,3):");
        } else {
            System.out.println("Turno del rival...");
        }
    }

    /**
     * Muestra por qué el servidor ha rechazado lo que hemos enviado.
     * Se enseña la explicación en castellano y, entre paréntesis, el código
     * del protocolo (útil en la demo). Si el motivo no lo conocemos, solo el código.
     */
    public static void mostrarRechazo(String codigo, MotivoRechazo motivo) {
        if (motivo != null) {
            System.out.println("No permitido: " + motivo.getDescripcion() + " (" + codigo + ")");
        } else {
            System.out.println("No permitido: " + codigo);
        }
    }

    public static void mostrarEmpate() {
        System.out.println("Empate!");
        mostrarOpcionesFinPartida();
    }

    public static void mostrarGanador(String ganador, boolean porAbandono) {
        System.out.println("Gana " + ganador + (porAbandono ? " (el rival ha abandonado)" : "") + ".");
        mostrarOpcionesFinPartida();
    }

    public static void mostrarDesconocido(String linea) {
        System.out.println("Servidor: " + linea);
    }

    public static void mostrarAyuda() {
        System.out.println("Comandos: fila,columna (del 1 al 3) para jugar | revancha | salir");
    }

    // ---- Tablero ----

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
    public static void dibujarTablero(String t) {
        System.out.println();
        System.out.println("     1   2   3"); // números de columna
        for (int fila = 0; fila < Tablero.LADO; fila++) {
            System.out.print("  " + (fila + 1) + " "); // número de fila
            for (int col = 0; col < Tablero.LADO; col++) {
                char c = t.charAt(fila * Tablero.LADO + col); // posición en el texto de 9 caracteres
                System.out.print(" " + (c == Tablero.VACIA ? ' ' : c) + " "); // el '.' se pinta como hueco
                if (col < Tablero.LADO - 1) {
                    System.out.print("|"); // separador entre columnas
                }
            }
            System.out.println();
            if (fila < Tablero.LADO - 1) {
                System.out.println("    ---+---+---"); // separador entre filas
            }
        }
        System.out.println();
    }

    // ---- Auxiliares ----

    private static void mostrarOpcionesFinPartida() {
        System.out.println("Escribe 'revancha' para jugar otra vez o 'salir' para terminar.");
    }
}
