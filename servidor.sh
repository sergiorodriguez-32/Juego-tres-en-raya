#!/bin/sh
# ------------------------------------------------------------------
#  servidor.sh - Arranca el servidor.
#  Uso:  ./servidor.sh           (puerto 5050)
#        ./servidor.sh 6000      (otro puerto)
#  Se para con Ctrl+C. Antes hay que compilar con ./compilar.sh
# ------------------------------------------------------------------
cd "$(dirname "$0")" || exit 1
java -cp out tresenraya.servidor.Servidor "$@"
