package tresenraya.comun;

/**
 * CLASE PROTOCOLO
 * ---------------
 * Reúne en un solo sitio TODAS las palabras del protocolo que se envían
 * cliente y servidor por el socket, y los valores por defecto de la conexión.
 *
 * ¿Por qué una clase aparte?
 *  - El cliente y el servidor tienen que usar exactamente las mismas palabras.
 *    Si cada uno escribiera "MOVE" a mano, un error de tipeo rompería la
 *    comunicación sin que el compilador avisara. Con constantes, si te
 *    equivocas al escribir Protocolo.MOVE el programa no compila.
 *  - Sirve como "chuleta" del protocolo para la memoria y la exposición.
 *
 * Formato general: cada mensaje es UNA línea de texto. La primera palabra es
 * el comando y el resto son sus datos, separados por un espacio.
 *
 *  CLIENTE -> SERVIDOR
 *   PLAY <nombre>      Pedir entrar en la partida
 *   MOVE <fila,col>    Jugar en una casilla (fila y columna del 1 al 3)
 *   REMATCH            Pedir revancha cuando ha terminado la partida
 *   BYE                Salir de la partida
 *
 *  SERVIDOR -> CLIENTE
 *   WAIT                         Espera al rival (o a que acepte la revancha)
 *   FULL                         Ya hay dos jugadores; se cierra la conexión
 *   START <X|O>                  Empieza la partida y te dice tu marca
 *   OK <tablero>                 Jugada aceptada + tablero en 9 caracteres
 *   TURN <X|O>                   A quién le toca mover
 *   DENIED <motivo>              Jugada rechazada (ver MotivoRechazo)
 *   END WIN <nombre> [ABANDONO]  Fin: gana <nombre> (por abandono del rival)
 *   END DRAW -                   Fin: empate
 *
 * La clase es "final" y su constructor privado porque no tiene sentido crear
 * objetos de ella: solo se usan sus constantes (Protocolo.PLAY, etc.).
 */
public final class Protocolo {

    private Protocolo() {
        // No se instancia: solo contiene constantes
    }

    // ---- Valores por defecto de la conexión ----

    /** Puerto que indica el enunciado si no se pasa otro por argumento. */
    public static final int PUERTO_POR_DEFECTO = 5050;

    /** "localhost" = este mismo ordenador. Para jugar en dos PCs se pasa la IP. */
    public static final String HOST_POR_DEFECTO = "localhost";

    /** Separador entre las partes de un mensaje. */
    public static final String SEPARADOR = " ";

    // ---- Mensajes que envía el CLIENTE ----

    public static final String PLAY = "PLAY";
    public static final String MOVE = "MOVE";
    public static final String REMATCH = "REMATCH";
    public static final String BYE = "BYE";

    // ---- Mensajes que envía el SERVIDOR ----

    public static final String WAIT = "WAIT";
    public static final String FULL = "FULL";
    public static final String START = "START";
    public static final String OK = "OK";
    public static final String TURN = "TURN";
    public static final String DENIED = "DENIED";
    public static final String END = "END";

    // ---- Partes del mensaje END ----

    public static final String WIN = "WIN";
    public static final String DRAW = "DRAW";
    /** Ocupa el hueco del ganador en "END DRAW -" para que END tenga siempre 3 partes. */
    public static final String SIN_GANADOR = "-";
    /** Cuarta parte opcional de "END WIN <nombre> ABANDONO". */
    public static final String ABANDONO = "ABANDONO";

    /**
     * Construye un mensaje juntando sus partes con espacios.
     * Ejemplos:
     *   mensaje(START, 'X')                  -> "START X"
     *   mensaje(DENIED, NO_ES_TU_TURNO)      -> "DENIED NO_ES_TU_TURNO"
     *   mensaje(END, WIN, "Sergio")          -> "END WIN Sergio"
     *
     * "Object... partes" significa que acepta cualquier número de datos de
     * cualquier tipo (String, char, enum, Tablero...). De cada uno se usa su
     * toString(), por eso el Tablero se convierte solo en sus 9 caracteres.
     */
    public static String mensaje(String comando, Object... partes) {
        StringBuilder sb = new StringBuilder(comando);
        for (Object parte : partes) {
            sb.append(SEPARADOR).append(parte);
        }
        return sb.toString();
    }
}
