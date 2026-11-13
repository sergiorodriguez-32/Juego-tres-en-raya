@echo off
rem ------------------------------------------------------------------
rem  compilar.bat - Compila todo el proyecto en la carpeta "out".
rem  Uso: doble clic, o escribir "compilar" en una terminal en esta carpeta.
rem
rem  Se compila en dos pasos:
rem   1. Servidor, cliente de consola y pruebas (solo necesitan Java).
rem   2. Cliente con ventana (necesita un Java que incluya JavaFX, por
rem      ejemplo Liberica JDK "Full"). Si falla, lo demas sigue valiendo y
rem      la ventana se puede abrir desde IntelliJ, que usa el pom.xml.
rem ------------------------------------------------------------------
pushd "%~dp0"
if exist out rmdir /s /q out
javac -encoding UTF-8 -d out ^
  src\main\java\tresenraya\comun\*.java ^
  src\main\java\tresenraya\juego\*.java ^
  src\main\java\tresenraya\servidor\*.java ^
  src\main\java\tresenraya\cliente\*.java ^
  src\test\java\tresenraya\juego\*.java
if errorlevel 1 (
  echo.
  echo *** Hay errores de compilacion ***
  goto fin
)
echo Servidor y cliente de consola compilados en la carpeta out

rem Copia los recursos (el estilo estilo.css de la ventana) a "out". Sin esto,
rem gui.bat no encuentra el CSS y la ventana se ve con el estilo por defecto.
if exist src\main\resources xcopy /e /i /y /q src\main\resources out >nul

javac -encoding UTF-8 -cp out -d out src\main\java\tresenraya\cliente\gui\*.java 2>nul
if errorlevel 1 (
  echo.
  echo AVISO: no se ha podido compilar el cliente con ventana.
  echo Este Java no incluye JavaFX. Abrelo desde IntelliJ: LanzadorGui - Run.
) else (
  echo Cliente con ventana compilado
)
:fin
popd
pause
