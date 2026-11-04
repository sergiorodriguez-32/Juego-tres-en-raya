@echo off
rem ------------------------------------------------------------------
rem  cliente.bat - Arranca un jugador.
rem  Uso:  cliente                      (servidor en este PC, puerto 5050)
rem        cliente 192.168.1.40         (servidor en otro PC)
rem        cliente 192.168.1.40 6000    (otro PC y otro puerto)
rem  Abre una ventana por cada jugador. Antes hay que compilar.
rem ------------------------------------------------------------------
pushd "%~dp0"
java -cp out tresenraya.cliente.Cliente %*
popd
pause
