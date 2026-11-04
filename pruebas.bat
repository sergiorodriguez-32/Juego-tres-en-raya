@echo off
rem ------------------------------------------------------------------
rem  pruebas.bat - Ejecuta las pruebas del tablero (PruebaTablero).
rem  Antes hay que compilar con compilar.bat.
rem ------------------------------------------------------------------
pushd "%~dp0"
java -cp out tresenraya.juego.PruebaTablero
popd
pause
