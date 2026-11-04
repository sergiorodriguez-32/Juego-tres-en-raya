/**
 * PAQUETE CLIENTE
 * ---------------
 * El programa que ejecuta cada jugador. Usa dos hilos y tres clases:
 *
 *  - {@link tresenraya.cliente.Cliente}: el main. Se conecta al servidor y,
 *    en el hilo principal, lee el teclado y envía los comandos.
 *  - {@link tresenraya.cliente.EscuchaServidor}: el hilo lector. Recibe los
 *    mensajes del servidor y decide qué mostrar.
 *  - {@link tresenraya.cliente.VistaConsola}: todo lo que se escribe en
 *    pantalla (tablero, turnos, errores, ayuda).
 *
 * Esquema:
 * <pre>
 *   teclado --> Cliente (hilo principal) --MOVE/REMATCH/BYE--> servidor
 *   pantalla <-- VistaConsola <-- EscuchaServidor (hilo lector) <--OK/TURN/END-- servidor
 * </pre>
 */
package tresenraya.cliente;
