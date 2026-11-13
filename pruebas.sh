#!/bin/sh
# ------------------------------------------------------------------
#  pruebas.sh - Ejecuta las pruebas del tablero (PruebaTablero).
#  Antes hay que compilar con ./compilar.sh
# ------------------------------------------------------------------
cd "$(dirname "$0")" || exit 1
java -cp out tresenraya.juego.PruebaTablero
