# Tres en raya online (Java, TCP)

Práctica de Programación multimedia y dispositivos móviles (2º DAM): un servidor y dos clientes que juegan al tres en raya por sockets TCP.

## Estructura del proyecto

```
src/
├── main/java/tresenraya/
│   ├── comun/                 Lo que comparten cliente y servidor
│   │   ├── Protocolo.java         Palabras del protocolo, puerto y host por defecto
│   │   └── MotivoRechazo.java     Motivos de DENIED (enum)
│   ├── juego/                 Reglas del juego, sin red ni hilos
│   │   └── Tablero.java
│   ├── servidor/              Programa del servidor
│   │   ├── Servidor.java          main: abre el puerto y lanza un hilo por cliente
│   │   ├── ManejadorJugador.java  Hilo de cada cliente: lee el socket y llama a Partida
│   │   └── Partida.java           Estado compartido (synchronized)
│   └── cliente/               Programa del jugador
│       ├── Cliente.java           main: conexión y teclado (hilo principal)
│       ├── EscuchaServidor.java   Hilo lector: interpreta lo que envía el servidor
│       └── VistaConsola.java      Todo lo que se pinta en pantalla
└── test/java/tresenraya/juego/
    └── PruebaTablero.java     Pruebas del tablero
```

Cada paquete tiene un `package-info.java` que explica para qué sirve.

## Cómo ejecutarlo

### Con los scripts (Windows)

1. `compilar.bat` compila todo en la carpeta `out`.
2. `pruebas.bat` (opcional) tiene que terminar con `Todas las pruebas OK`.
3. `servidor.bat` arranca el servidor en el puerto 5050 (`servidor 6000` para otro puerto).
4. `cliente.bat` en una ventana por jugador. Si el servidor está en otro PC: `cliente 192.168.1.40`.

### A mano en la consola

```
javac -encoding UTF-8 -d out src/main/java/tresenraya/comun/*.java src/main/java/tresenraya/juego/*.java src/main/java/tresenraya/servidor/*.java src/main/java/tresenraya/cliente/*.java src/test/java/tresenraya/juego/*.java
java -cp out tresenraya.juego.PruebaTablero
java -cp out tresenraya.servidor.Servidor [puerto]
java -cp out tresenraya.cliente.Cliente [ip] [puerto]
```

### Desde IntelliJ

Clic derecho en `Servidor` y luego *Run*. Haz lo mismo con `Cliente`. Para abrir varios clientes, ve a *Run → Edit Configurations → Cliente → Modify options* y marca *Allow multiple instances*.

## Protocolo

Cada mensaje es una línea de texto: el comando y, detrás, sus datos separados por espacios.

| Cliente → Servidor | Significado |
|---|---|
| `PLAY <nombre>` | Entrar en la partida |
| `MOVE <fila,col>` | Jugar en una casilla (del 1 al 3) |
| `REMATCH` | Pedir revancha |
| `BYE` | Salir |

| Servidor → Cliente | Significado |
|---|---|
| `WAIT` | Esperando al rival o a que acepte la revancha |
| `FULL` | Ya hay dos jugadores; se cierra la conexión |
| `START <X\|O>` | Empieza la partida y te dice tu marca |
| `OK <tablero>` | Jugada aceptada; tablero en 9 caracteres (`X..O.....`) |
| `TURN <X\|O>` | A quién le toca |
| `DENIED <motivo>` | Jugada rechazada (ver `MotivoRechazo`) |
| `END WIN <nombre> [ABANDONO]` | Gana `<nombre>`, por abandono si va `ABANDONO` |
| `END DRAW -` | Empate |
