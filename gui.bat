@echo off
rem ------------------------------------------------------------------
rem  gui.bat - Arranca un jugador con ventana (JavaFX).
rem  Uso:  gui                    (campos rellenos con localhost y 5050)
rem        gui 192.168.1.40       (rellena la IP del servidor)
rem  Antes hay que compilar con compilar.bat.
rem ------------------------------------------------------------------
pushd "%~dp0"
java -cp out tresenraya.cliente.gui.LanzadorGui %*
rem Si ha habido un error, esperamos para que se pueda leer
if errorlevel 1 pause
popd
