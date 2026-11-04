@echo off
rem ------------------------------------------------------------------
rem  servidor.bat - Arranca el servidor.
rem  Uso:  servidor           (puerto 5050)
rem        servidor 6000      (otro puerto)
rem  Se para con Ctrl+C. Antes hay que compilar con compilar.bat.
rem ------------------------------------------------------------------
pushd "%~dp0"
java -cp out tresenraya.servidor.Servidor %*
popd
pause
