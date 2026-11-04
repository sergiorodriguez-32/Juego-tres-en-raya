package tresenraya.comun;

/**
 * ENUM MOTIVORECHAZO
 * ------------------
 * Los motivos que puede llevar un mensaje "DENIED <motivo>".
 *
 * Un enum es un tipo con una lista CERRADA de valores. Así el servidor no
 * puede mandar un motivo inventado o mal escrito: solo uno de estos.
 * Al enviarlo, Java usa su nombre tal cual (NO_ES_TU_TURNO), que es justo
 * el texto que viaja por la red.
 *
 * Cada valor lleva además una descripción en castellano, que es la que el
 * cliente enseña al jugador en vez del código. Va sin tildes, igual que el
 * resto de textos de la consola, para que se vea bien en cualquier terminal.
 */
public enum MotivoRechazo {

    NO_ES_TU_TURNO("no es tu turno"),
    CASILLA_OCUPADA("esa casilla ya esta ocupada"),
    FUERA_DE_RANGO("la fila y la columna van del 1 al 3"),
    FORMATO_INCORRECTO("escribe la jugada como fila,columna (por ejemplo 2,3)"),
    PARTIDA_TERMINADA("la partida ha terminado"),
    ESPERANDO_RIVAL("todavia no ha llegado tu rival"),
    NO_HAY_PARTIDA_TERMINADA("solo puedes pedir revancha cuando termina la partida"),
    COMANDO_DESCONOCIDO("comando desconocido"),
    DEBES_EMPEZAR_CON_PLAY("primero tienes que entrar con PLAY <nombre>");

    /** Texto para el jugador. */
    private final String descripcion;

    MotivoRechazo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Pasa del texto recibido por la red al valor del enum.
     * Devuelve null si el servidor manda un motivo que no conocemos,
     * en lugar de lanzar una excepción como haría valueOf().
     */
    public static MotivoRechazo desdeTexto(String texto) {
        for (MotivoRechazo m : values()) {
            if (m.name().equals(texto)) {
                return m;
            }
        }
        return null;
    }
}
