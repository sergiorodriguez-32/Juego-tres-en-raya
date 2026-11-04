/**
 * PAQUETE SERVIDOR
 * ----------------
 * El programa que se ejecuta en el ordenador que hace de servidor.
 *
 *  - {@link tresenraya.servidor.Servidor}: el main. Abre el puerto y lanza un
 *    hilo por cada cliente que se conecta.
 *  - {@link tresenraya.servidor.ManejadorJugador}: el hilo de cada cliente.
 *    Lee sus mensajes del socket y se los pasa a la Partida.
 *  - {@link tresenraya.servidor.Partida}: el estado compartido (tablero,
 *    turno, jugadores). La usan los dos hilos a la vez, por eso todos sus
 *    métodos públicos son synchronized.
 *
 * Esquema:
 * <pre>
 *   Cliente 1 --socket--> ManejadorJugador (hilo 1) --\
 *                                                      >-- Partida (1 objeto) -- Tablero
 *   Cliente 2 --socket--> ManejadorJugador (hilo 2) --/
 * </pre>
 */
package tresenraya.servidor;
