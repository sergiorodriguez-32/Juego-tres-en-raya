package tresenraya.cliente;

import java.io.BufferedReader; // leer texto del servidor línea a línea
import java.io.IOException;    // error de red

import tresenraya.comun.MotivoRechazo;

import static tresenraya.comun.Protocolo.*;

/**
 * CLASE ESCUCHASERVIDOR
 * ---------------------
 * Es el código del hilo LECTOR del cliente. Implementa Runnable para poder
 * ejecutarlo con new Thread(escucha).start().
 *
 * Se pasa todo el rato leyendo lo que envía el servidor (readLine se queda
 * esperando hasta que llega una línea), interpreta cada mensaje según el
 * protocolo y le pide a VistaConsola que lo muestre.
 *
 * Termina cuando el servidor cierra la conexión (readLine devuelve null).
 */
public class EscuchaServidor implements Runnable {

    // Por donde llegan los mensajes del servidor
    private final BufferedReader entrada;

    // Marca de este jugador ('X' u 'O'). La recibe del servidor con START.
    // Solo la usa este hilo, pero la marcamos volatile por si en el futuro la
    // lee también el hilo principal (volatile = todos los hilos ven el valor actual).
    private volatile char miMarca = '?';

    // Se pone a true cuando el usuario escribe "salir". La ESCRIBE el hilo
    // principal y la LEE este hilo, por eso tiene que ser volatile: sin él, este
    // hilo podría seguir viendo el valor antiguo (false).
    private volatile boolean saliendo = false;

    public EscuchaServidor(BufferedReader entrada) {
        this.entrada = entrada;
    }

    /**
     * Lo llama el hilo principal justo antes de enviar BYE, para que al
     * cerrarse la conexión no se muestre "Conexion cerrada por el servidor".
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
        // Si la conexión se cierra sin que el usuario haya escrito "salir",
        // es que la ha cerrado el servidor (por ejemplo, tras FULL): cerramos el programa.
        if (!saliendo) {
            VistaConsola.mostrarConexionCerrada();
            System.exit(0);
        }
    }

    /**
     * Interpreta un mensaje del servidor según el protocolo y decide qué
     * mostrar al jugador.
     */
    private void procesarMensaje(String linea) {
        // Partimos el mensaje por espacios. Ej: "END WIN Sergio" -> ["END", "WIN", "Sergio"]
        String[] partes = linea.split(SEPARADOR);

        switch (partes[0]) {
            // WAIT: estamos esperando a que llegue el rival (o a que acepte la revancha)
            case WAIT -> VistaConsola.mostrarEsperando();

            // FULL: ya hay dos jugadores; el servidor cerrará la conexión
            case FULL -> VistaConsola.mostrarPartidaLlena();

            // START X / START O: empieza la partida y nos dice nuestra marca
            case START -> {
                miMarca = partes[1].charAt(0);
                VistaConsola.mostrarInicio(miMarca);
            }

            // OK <tablero>: jugada aceptada; nos llega el tablero actualizado
            case OK -> VistaConsola.dibujarTablero(partes[1]);

            // TURN X / TURN O: de quién es el turno ahora
            case TURN -> VistaConsola.mostrarTurno(partes[1].charAt(0) == miMarca);

            // DENIED <motivo>: lo que hemos pedido no es válido (no es nuestro turno, casilla ocupada...)
            case DENIED -> VistaConsola.mostrarRechazo(partes[1], MotivoRechazo.desdeTexto(partes[1]));

            // END WIN <ganador> [ABANDONO] / END DRAW -: fin de la partida
            case END -> {
                if (partes[1].equals(DRAW)) {
                    VistaConsola.mostrarEmpate();
                } else {
                    String ganador = partes[2];
                    // Si hay una cuarta palabra "ABANDONO", el rival se ha ido
                    boolean abandono = partes.length > 3 && partes[3].equals(ABANDONO);
                    VistaConsola.mostrarGanador(ganador, abandono);
                }
            }

            // Cualquier otro mensaje que no conozcamos: lo mostramos tal cual
            default -> VistaConsola.mostrarDesconocido(linea);
        }
    }
}
