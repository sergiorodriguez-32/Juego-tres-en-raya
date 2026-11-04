/**
 * PAQUETE CLIENTE.GUI
 * -------------------
 * El cliente con ventana (JavaFX). Es otra forma de jugar, con el mismo
 * protocolo y el mismo servidor: un jugador puede usar la ventana y el otro la
 * consola, y juegan entre ellos sin problema.
 *
 *  - {@link tresenraya.cliente.gui.LanzadorGui}: el main.
 *  - {@link tresenraya.cliente.gui.AplicacionGui}: la aplicación JavaFX; cambia
 *    de pantalla y abre y cierra la conexión.
 *  - {@link tresenraya.cliente.gui.PantallaConexion}: formulario de nombre, IP y puerto.
 *  - {@link tresenraya.cliente.gui.PantallaJuego}: el tablero. Implementa
 *    {@link tresenraya.cliente.Vista}, igual que VistaConsola.
 *
 * Reutiliza del paquete cliente la Conexion y el EscuchaServidor: el código de
 * red es el mismo para la consola y para la ventana.
 */
package tresenraya.cliente.gui;
