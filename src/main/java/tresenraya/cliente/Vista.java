package tresenraya.cliente;

import tresenraya.comun.MotivoRechazo;

/**
 * INTERFAZ VISTA
 * --------------
 * Lo que tiene que saber hacer cualquier "pantalla" del cliente para enseñar
 * al jugador lo que va pasando en la partida.
 *
 * Hay dos implementaciones:
 *  - VistaConsola: escribe en la terminal (cliente de texto).
 *  - PantallaJuego (paquete gui): pinta en una ventana de JavaFX.
 *
 * EscuchaServidor recibe los mensajes del servidor y llama a estos métodos
 * SIN SABER cuál de las dos pantallas hay detrás. Eso es polimorfismo: el
 * mismo código de red sirve para la consola y para la ventana.
 *
 * OJO: estos métodos los llama el hilo LECTOR (EscuchaServidor), no el hilo
 * principal. En consola da igual, pero en JavaFX importa (ver PantallaJuego).
 */
public interface Vista {

    /** WAIT: esperando a que llegue el rival o a que acepte la revancha. */
    void mostrarEsperando();

    /** FULL: ya hay dos jugadores; el servidor va a cerrar la conexión. */
    void mostrarPartidaLlena();

    /** START: empieza una partida y nos dice con qué marca jugamos. */
    void mostrarInicio(char miMarca);

    /** OK: el tablero ha cambiado. Llega como texto de 9 caracteres ("X...O...."). */
    void mostrarTablero(String tablero);

    /** TURN: cambia el turno. meToca es true si nos toca a nosotros. */
    void mostrarTurno(boolean meToca);

    /**
     * DENIED: el servidor ha rechazado lo que hemos enviado.
     * codigo es el texto recibido y motivo su valor en el enum (null si es desconocido).
     */
    void mostrarRechazo(String codigo, MotivoRechazo motivo);

    /** END DRAW: la partida ha terminado en empate. */
    void mostrarEmpate();

    /**
     * END WIN: la partida ha terminado y ha ganado 'ganador'.
     * yoGano dice si el ganador somos nosotros. Lo calcula EscuchaServidor por
     * el turno y no comparando nombres, porque dos jugadores podrían haber
     * escrito el mismo nombre y los dos creerían que han ganado.
     */
    void mostrarGanador(String ganador, boolean porAbandono, boolean yoGano);

    /** Un mensaje que no sabemos interpretar. */
    void mostrarDesconocido(String linea);

    /** El servidor ha cerrado la conexión sin que nosotros pidiéramos salir. */
    void conexionCerrada();
}
