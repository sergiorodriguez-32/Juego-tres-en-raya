@echo off
rem ------------------------------------------------------------------
rem  compilar.bat - Compila todo el proyecto en la carpeta "out".
rem  Uso: doble clic, o escribir "compilar" en una terminal en esta carpeta.
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
) else (
  echo Compilado correctamente en la carpeta out
)
popd
pause
