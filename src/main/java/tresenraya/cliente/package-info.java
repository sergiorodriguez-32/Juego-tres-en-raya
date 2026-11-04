/**
 * PAQUETE CLIENTE
 * ---------------
 * El programa que ejecuta cada jugador. Usa dos hilos:
 *
 *  - {@link tresenraya.cliente.Conexion}: el socket con el servidor. Envía los
 *    comandos (PLAY, MOVE, REMATCH, BYE) y arranca el hilo lector.
 *  - {@link tresenraya.cliente.EscuchaServidor}: el hilo lector. Recibe los
 *    mensajes del servidor y le dice a la Vista qué mostrar.
 *  - {@link tresenraya.cliente.Vista}: interfaz con lo que tiene que saber
 *    hacer cualquier pantalla (mostrar el tablero, el turno, el ganador...).
 *  - {@link tresenraya.cliente.VistaConsola}: la Vista en modo texto.
 *  - {@link tresenraya.cliente.Cliente}: el main del cliente de consola. Lee
 *    el teclado en el hilo principal.
 *
 * La versión con ventana está en el subpaquete gui y reutiliza Conexion,
 * EscuchaServidor y Vista.
 *
 * Esquema:
 * <pre>
 *   teclado o clics --> Conexion --MOVE/REMATCH/BYE--> servidor
 *   pantalla <-- Vista <-- EscuchaServidor (hilo lector) <--OK/TURN/END-- servidor
 *                  |
 *                  +-- VistaConsola (texto) o PantallaJuego (JavaFX)
 * </pre>
 */
package tresenraya.cliente;
