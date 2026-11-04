package tresenraya.cliente;

import java.util.Scanner;

import tresenraya.comun.MotivoRechazo;
import tresenraya.juego.Tablero;

/**
 * CLASE VISTACONSOLA
 * ------------------
 * La "pantalla" del cliente de texto: TODO lo que se escribe en la terminal
 * está aquí (el tablero, los avisos de turno, los errores, la ayuda...).
 *
 * Implementa la interfaz Vista, así EscuchaServidor puede usarla sin saber
 * que es una consola. La versión con ventana es gui.PantallaJuego.
 *
 * ¿Por qué separarlo?
 *  - Cliente, Conexion y EscuchaServidor se ocupan solo de la comunicación;
 *    no tienen ningún System.out.println mezclado con la lógica.
 *  - Si queremos cambiar un texto o el dibujo del tablero, solo hay que
 *    mirar esta clase.
 */
public class VistaConsola implements Vista {

    // =================================================================
    // Métodos de la interfaz Vista (los llama el hilo lector)
    // =================================================================

    @Override
    public void mostrarEsperando() {
        System.out.println("Esperando al otro jugador...");
    }

    @Override
    public void mostrarPartidaLlena() {
        System.out.println("La partida esta llena. Intentalo mas tarde.");
    }

    @Override
    public void mostrarInicio(char miMarca) {
        System.out.println("\nEmpieza la partida! Juegas con " + miMarca);
        mostrarTablero(new Tablero().toString()); // tablero vacío: "........."
    }

    @Override
    public void mostrarTurno(boolean meToca) {
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
    @Override
    public void mostrarRechazo(String codigo, MotivoRechazo motivo) {
        if (motivo != null) {
            System.out.println("No permitido: " + motivo.getDescripcion() + " (" + codigo + ")");
        } else {
            System.out.println("No permitido: " + codigo);
        }
    }

    @Override
    public void mostrarEmpate() {
        System.out.println("Empate!");
        mostrarOpcionesFinPartida();
    }

    @Override
    public void mostrarGanador(String ganador, boolean porAbandono) {
        System.out.println("Gana " + ganador + (porAbandono ? " (el rival ha abandonado)" : "") + ".");
        mostrarOpcionesFinPartida();
    }

    @Override
    public void mostrarDesconocido(String linea) {
        System.out.println("Servidor: " + linea);
    }

    /** El servidor nos ha echado (por ejemplo, tras FULL): cerramos el programa. */
    @Override
    public void conexionCerrada() {
        System.out.println("\nConexion cerrada por el servidor.");
        System.exit(0);
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
    @Override
    public void mostrarTablero(String t) {
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

    // =================================================================
    // Métodos propios del cliente de consola (los llama Cliente)
    // =================================================================

    public String pedirNombre(Scanner teclado) {
        System.out.print("Tu nombre: ");
        return teclado.nextLine().trim();
    }

    public void mostrarConectado(String host, int puerto) {
        System.out.println("Conectado a " + host + ":" + puerto);
    }

    public void mostrarPuertoNoValido(int puertoPorDefecto) {
        System.out.println("Puerto no valido, uso el " + puertoPorDefecto);
    }

    public void mostrarErrorConexion(String detalle) {
        System.out.println("No se pudo conectar con el servidor: " + detalle);
    }

    public void mostrarDespedida() {
        System.out.println("Hasta luego.");
    }

    public void mostrarAyuda() {
        System.out.println("Comandos: fila,columna (del 1 al 3) para jugar | revancha | salir");
    }

    private void mostrarOpcionesFinPartida() {
        System.out.println("Escribe 'revancha' para jugar otra vez o 'salir' para terminar.");
    }
}
