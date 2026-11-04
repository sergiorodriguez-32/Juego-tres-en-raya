package tresenraya.cliente.gui;

import javafx.application.Application;

/**
 * CLASE LANZADORGUI
 * -----------------
 * Punto de entrada del cliente con ventana. Se ejecuta con:
 *     java -cp out tresenraya.cliente.gui.LanzadorGui [ip] [puerto]
 * (o con gui.bat, o con Run desde IntelliJ). La IP y el puerto son
 * opcionales: solo rellenan de antemano los campos de la primera pantalla.
 *
 * ¿Por qué una clase aparte solo para el main?
 * Si la clase que tiene el main hereda de Application, Java comprueba al
 * arrancar que JavaFX está cargado "como módulo", y en un proyecto normal
 * (sin module-info.java) falla con "JavaFX runtime components are missing".
 * Con un main en una clase que NO hereda de Application esa comprobación no se
 * hace y todo funciona. Es un truco muy habitual en proyectos con JavaFX.
 */
public class LanzadorGui {

    public static void main(String[] args) {
        // launch() crea el hilo de JavaFX, construye un AplicacionGui y llama a su start()
        Application.launch(AplicacionGui.class, args);
    }
}
