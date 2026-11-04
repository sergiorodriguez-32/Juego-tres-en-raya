package tresenraya.juego;

/**
 * CLASE PRUEBATABLERO
 * -------------------
 * Pruebas rápidas de la clase Tablero, sin red ni hilos.
 * Sirve para comprobar que las reglas del juego funcionan ANTES de montar
 * el servidor. Si aquí falla algo, en la partida también fallará.
 *
 * Está en src/test/java porque es código de prueba, no parte del programa:
 * en Maven el código de prueba va separado del código que se entrega.
 * Está en el mismo paquete que Tablero (tresenraya.juego), así que no
 * necesita import para usarlo.
 *
 * Cómo usarla: ejecutar su main (o pruebas.bat). Si todo está bien, imprime
 * una línea "OK" por cada prueba y al final "Todas las pruebas OK". Si alguna
 * falla, el programa se para con un error que dice qué prueba ha fallado.
 */
public class PruebaTablero {

    public static void main(String[] args) {

        // ---- Prueba 1: victoria en una fila ----
        // Marcamos la fila 2 entera con X:  . . . / X X X / . . .
        Tablero t = new Tablero();
        t.marcar(2, 1, Tablero.X);
        t.marcar(2, 2, Tablero.X);
        t.marcar(2, 3, Tablero.X);
        comprobar(t.ganador() == Tablero.X, "victoria en fila");

        // ---- Prueba 2: victoria en una columna ----
        // Marcamos la columna 3 entera con O
        t = new Tablero();
        t.marcar(1, 3, Tablero.O);
        t.marcar(2, 3, Tablero.O);
        t.marcar(3, 3, Tablero.O);
        comprobar(t.ganador() == Tablero.O, "victoria en columna");

        // ---- Prueba 3: victoria en la diagonal de arriba-derecha a abajo-izquierda ----
        t = new Tablero();
        t.marcar(1, 3, Tablero.X);
        t.marcar(2, 2, Tablero.X);
        t.marcar(3, 1, Tablero.X);
        comprobar(t.ganador() == Tablero.X, "victoria en diagonal");

        // ---- Prueba 4: empate (tablero lleno y nadie con tres en línea) ----
        //  X O X
        //  X O O
        //  O X X
        t = new Tablero();
        String empate = "XOXXOOOXX"; // el tablero de arriba escrito fila a fila
        for (int i = 0; i < Tablero.NUM_CASILLAS; i++) {
            // i / 3 da la fila (0,1,2) e i % 3 la columna (0,1,2); sumamos 1 para pasar a 1-3
            t.marcar(i / 3 + 1, i % 3 + 1, empate.charAt(i));
        }
        comprobar(t.estaLleno() && !t.hayGanador(), "empate");

        // ---- Prueba 5: casilla ocupada ----
        t = new Tablero();
        t.marcar(1, 1, Tablero.X);
        comprobar(!t.estaLibre(1, 1), "casilla ocupada");

        // ---- Prueba 6: casillas fuera del tablero ----
        comprobar(!t.estaDentro(0, 2) && !t.estaDentro(4, 1), "fuera de rango");

        // ---- Prueba 7: formato de texto que se envía en el mensaje OK ----
        // Solo hay una X en la casilla (1,1), que es la primera posición
        comprobar(t.toString().equals("X........"), "toString");

        // ---- Prueba 8: cambio de turno ----
        comprobar(Tablero.contraria(Tablero.X) == Tablero.O
                && Tablero.contraria(Tablero.O) == Tablero.X, "cambio de turno");

        System.out.println("Todas las pruebas OK");
    }

    /**
     * Método auxiliar: si la condición es falsa, para el programa con un error
     * que dice qué prueba ha fallado; si es verdadera, imprime "OK".
     */
    private static void comprobar(boolean condicion, String nombre) {
        if (!condicion) {
            throw new AssertionError("FALLA: " + nombre);
        }
        System.out.println("OK  " + nombre);
    }
}
