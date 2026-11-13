#!/bin/sh
# ------------------------------------------------------------------
#  compilar.sh - Compila todo el proyecto en la carpeta "out".
#  Uso: ./compilar.sh
#
#  Se compila en dos pasos:
#   1. Servidor, cliente de consola y pruebas (solo necesitan Java).
#   2. Cliente con ventana (necesita un Java que incluya JavaFX, por
#      ejemplo Liberica JDK "Full"). Si falla, lo demas sigue valiendo y
#      la ventana se puede abrir desde IntelliJ, que usa el pom.xml.
# ------------------------------------------------------------------
cd "$(dirname "$0")" || exit 1
rm -rf out

if ! javac -encoding UTF-8 -d out \
  src/main/java/tresenraya/comun/*.java \
  src/main/java/tresenraya/juego/*.java \
  src/main/java/tresenraya/servidor/*.java \
  src/main/java/tresenraya/cliente/*.java \
  src/test/java/tresenraya/juego/*.java; then
  echo
  echo "*** Hay errores de compilacion ***"
  exit 1
fi
echo "Servidor y cliente de consola compilados en la carpeta out"

# Copia los recursos (el estilo estilo.css de la ventana) a "out". Sin esto,
# gui.sh no encuentra el CSS y la ventana se ve con el estilo por defecto.
if [ -d src/main/resources ]; then
  cp -r src/main/resources/. out/
fi

if javac -encoding UTF-8 -cp out -d out src/main/java/tresenraya/cliente/gui/*.java 2>/dev/null; then
  echo "Cliente con ventana compilado"
else
  echo
  echo "AVISO: no se ha podido compilar el cliente con ventana."
  echo "Este Java no incluye JavaFX. Abrelo desde IntelliJ: LanzadorGui - Run."
fi
