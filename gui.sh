#!/bin/sh
# ------------------------------------------------------------------
#  gui.sh - Arranca un jugador con ventana (JavaFX).
#  Uso:  ./gui.sh                    (campos rellenos con localhost y 5050)
#        ./gui.sh 192.168.1.40       (rellena la IP del servidor)
#  Antes hay que compilar con ./compilar.sh
# ------------------------------------------------------------------
cd "$(dirname "$0")" || exit 1
java -cp out tresenraya.cliente.gui.LanzadorGui "$@"
