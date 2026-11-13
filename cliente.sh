#!/bin/sh
# ------------------------------------------------------------------
#  cliente.sh - Arranca un jugador (consola).
#  Uso:  ./cliente.sh                      (servidor en este PC, puerto 5050)
#        ./cliente.sh 192.168.1.40         (servidor en otro PC)
#        ./cliente.sh 192.168.1.40 6000    (otro PC y otro puerto)
#  Abre una terminal por cada jugador. Antes hay que compilar.
# ------------------------------------------------------------------
cd "$(dirname "$0")" || exit 1
java -cp out tresenraya.cliente.Cliente "$@"
