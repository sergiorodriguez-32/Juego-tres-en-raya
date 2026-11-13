package tresenraya.cliente;

import java.io.BufferedReader; // leer texto del servidor línea a línea
import java.io.IOException;    // error de red

import tresenraya.comun.MotivoRechazo;

import static tresenraya.comun.Protocolo.*;

/**
 * CLASE ESCUCHASERVIDOR
 * ---------------------
 * Es el código del hilo LECTOR del cliente. Implementa Runnable para poder
 * ejecutarlo con new Thread(escucha).start() (lo hace la clase Conexion).
 *
 * Se pasa todo el rato leyendo lo que envía el servidor (readLine se queda
 * esperando hasta que llega una línea), interpreta cada mensaje según el
 * protocolo y le pide a la Vista que lo muestre.
 *
 * No sabe si la Vista es la consola o la ventana de JavaFX: solo usa los
 * métodos de la interfaz Vista.
 *
 * Termina cuando el servidor cierra la conexión (readLine devuelve null).
 */
public class EscuchaServidor implements Runnable {

    // Por donde llegan los mensajes del servidor
    private final BufferedReader entrada;

    // Dónde se enseña lo que llega (consola o ventana)
    private final Vista vista;

    // Marca de este jugador ('X' u 'O'). La recibe del servidor con START.
    // Solo la usa este hilo.
    private char miMarca = '?';

    // Marca a la que le tocaba mover según el último TURN recibido. Tras la
    // jugada ganadora el servidor no manda otro TURN, así que al llegar END WIN
    // esta marca es la del jugador que acaba de ganar. Con ella sabemos si hemos
    // ganado nosotros sin comparar nombres (dos jugadores pueden llamarse igual).
    private char turnoActual = '?';

    // Se pone a true cuando el usuario pide salir. La ESCRIBE otro hilo
    // (el principal o el de la ventana) y la LEE este hilo, por eso tiene que
    // ser volatile: sin él, este hilo podría seguir viendo el valor antiguo.
    private volatile boolean saliendo = false;

    public EscuchaServidor(BufferedReader entrada, Vista vista) {
        this.entrada = entrada;
        this.vista = vista;
    }

    /**
     * Lo llama Conexion.salir() justo antes de enviar BYE, para que al
     * cerrarse la conexión no se trate como un corte del servidor.
     */
    public void avisarDeSalida() {
        saliendo = true;
    }

    /** Bucle del hilo lector: lee del servidor y procesa cada mensaje. */
    @Override
    public void run() {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                procesarMensaje(linea);
            }
        } catch (IOException e) {
            // El socket se ha cerrado: no hace falta hacer nada más
        }
        // Si la conexión se cierra sin que el usuario haya pedido salir,
        // es que la ha cerrado el servidor (por ejemplo, tras FULL).
        if (!saliendo) {
            vista.conexionCerrada();
        }
    }

    /**
     * Interpreta un mensaje del servidor según el protocolo y decide qué
     * método de la Vista llamar.
     */
    private void procesarMensaje(String linea) {
        // Partimos el mensaje por espacios. Ej: "END WIN Sergio" -> ["END", "WIN", "Sergio"]
        String[] partes = linea.split(SEPARADOR);

        switch (partes[0]) {
            // WAIT: estamos esperando a que llegue el rival (o a que acepte la revancha)
            case WAIT -> vista.mostrarEsperando();

            // FULL: ya hay dos jugadores; el servidor cerrará la conexión
            case FULL -> vista.mostrarPartidaLlena();

            // START X / START O: empieza la partida y nos dice nuestra marca
            case START -> {
                miMarca = partes[1].charAt(0);
                vista.mostrarInicio(miMarca);
            }

            // OK <tablero>: jugada aceptada; nos llega el tablero actualizado
            case OK -> vista.mostrarTablero(partes[1]);

            // TURN X / TURN O: de quién es el turno ahora
            case TURN -> {
                turnoActual = partes[1].charAt(0);
                vista.mostrarTurno(turnoActual == miMarca);
            }

            // DENIED <motivo>: lo que hemos pedido no es válido (no es nuestro turno, casilla ocupada...)
            case DENIED -> vista.mostrarRechazo(partes[1], MotivoRechazo.desdeTexto(partes[1]));

            // END WIN <ganador> [ABANDONO] / END DRAW -: fin de la partida
            case END -> {
                if (partes[1].equals(DRAW)) {
                    vista.mostrarEmpate();
                } else {
                    String ganador = partes[2];
                    // Si hay una cuarta palabra "ABANDONO", el rival se ha ido
                    boolean abandono = partes.length > 3 && partes[3].equals(ABANDONO);
                    // Por abandono, END WIN solo le llega al que se queda: ha ganado él.
                    // Si no, gana quien movió por última vez (el del último TURN).
                    boolean yoGano = abandono || turnoActual == miMarca;
                    vista.mostrarGanador(ganador, abandono, yoGano);
                }
            }

            // Cualquier otro mensaje que no conozcamos: lo mostramos tal cual
            default -> vista.mostrarDesconocido(linea);
        }
    }
}
