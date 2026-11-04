// Arrays es una clase de utilidades de Java para trabajar con arrays.
// La usamos para rellenar el tablero entero de casillas vacías de una vez.
import java.util.Arrays;

/**
 * CLASE TABLERO
 * -------------
 * Representa el tablero de 3x3 del tres en raya y contiene SOLO las reglas
 * del juego: marcar casillas, comprobar si una casilla es válida, saber si
 * alguien ha ganado y saber si hay empate.
 *
 * Esta clase NO sabe nada de red, sockets ni hilos. Así la podemos probar
 * sola (con PruebaTablero) antes de montar el servidor.
 *
 * Las 9 casillas se guardan en un array de 9 posiciones, recorriendo el
 * tablero fila a fila:
 *
 *     columna:  1   2   3
 *   fila 1 ->   0 | 1 | 2
 *   fila 2 ->   3 | 4 | 5
 *   fila 3 ->   6 | 7 | 8
 *
 * Una casilla vacía se guarda como '.', y las ocupadas como 'X' u 'O'.
 * Así, el tablero completo se puede enviar por red como un texto de 9
 * caracteres, por ejemplo "X.O..X..." (es lo que pide el protocolo en OK).
 */
public class Tablero {

    // Carácter que usamos para una casilla vacía.
    // "public static final" = constante accesible desde otras clases (Tablero.VACIA).
    public static final char VACIA = '.';

    // Las 8 combinaciones de casillas que hacen "tres en raya".
    // Cada fila de este array son las 3 posiciones (del 0 al 8) de una línea.
    private static final int[][] LINEAS = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // las 3 filas
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // las 3 columnas
            {0, 4, 8}, {2, 4, 6}             // las 2 diagonales
    };

    // El tablero en sí: 9 caracteres ('.', 'X' u 'O').
    // Es "final" porque el array siempre es el mismo; solo cambia su contenido.
    private final char[] casillas = new char[9];

    /** Constructor: crea un tablero nuevo con todas las casillas vacías. */
    public Tablero() {
        reiniciar();
    }

    /**
     * Pone todas las casillas a vacío.
     * Se usa al crear el tablero y al empezar una revancha.
     */
    public void reiniciar() {
        Arrays.fill(casillas, VACIA); // equivale a un for que pone '.' en las 9 posiciones
    }

    /**
     * Comprueba que la casilla existe en el tablero.
     * fila y col van de 1 a 3, que es como las escribe el jugador.
     * Ejemplo: (2,3) es válida; (0,1) o (4,2) no.
     */
    public boolean estaDentro(int fila, int col) {
        return fila >= 1 && fila <= 3 && col >= 1 && col <= 3;
    }

    /** Devuelve true si nadie ha marcado todavía esa casilla. */
    public boolean estaLibre(int fila, int col) {
        return casillas[indice(fila, col)] == VACIA;
    }

    /**
     * Pone la marca ('X' u 'O') en la casilla indicada.
     * OJO: no comprueba nada. Antes de llamarlo hay que comprobar
     * estaDentro() y estaLibre() (eso lo hace la clase Partida).
     */
    public void marcar(int fila, int col, char marca) {
        casillas[indice(fila, col)] = marca;
    }

    /**
     * Mira si alguien tiene tres en línea.
     * Devuelve 'X' u 'O' si hay ganador, o VACIA ('.') si todavía no hay.
     */
    public char ganador() {
        // Recorremos las 8 líneas posibles
        for (int[] l : LINEAS) {
            char c = casillas[l[0]]; // lo que hay en la primera casilla de la línea
            // Hay ganador si la primera casilla NO está vacía
            // y las otras dos tienen la misma marca
            if (c != VACIA && c == casillas[l[1]] && c == casillas[l[2]]) {
                return c;
            }
        }
        return VACIA; // ninguna línea completa
    }

    /**
     * Devuelve true si ya no queda ninguna casilla libre.
     * Si el tablero está lleno y nadie ha ganado, es empate.
     */
    public boolean estaLleno() {
        for (char c : casillas) {
            if (c == VACIA) {
                return false; // en cuanto encontramos una vacía, no está lleno
            }
        }
        return true;
    }

    /**
     * Convierte el tablero en el texto de 9 caracteres que se envía en el
     * mensaje OK del protocolo. Ejemplo: "X.O..X...".
     * Al sobrescribir toString(), podemos escribir "OK " + tablero directamente.
     */
    @Override
    public String toString() {
        return new String(casillas);
    }

    /**
     * Convierte (fila, col) del 1 al 3 en la posición del array (del 0 al 8).
     * Restamos 1 porque los arrays empiezan en 0.
     * Ejemplo: fila 2, col 3 -> (2-1)*3 + (3-1) = 3 + 2 = 5.
     */
    private int indice(int fila, int col) {
        return (fila - 1) * 3 + (col - 1);
    }
}