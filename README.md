# Java Client-Server Chat

A simple console-based chat application built with Java TCP sockets. Multiple clients can connect to one server, register nicknames, exchange messages, and disconnect with the `/quit` command.

## Features

- TCP communication using Java `ServerSocket` and `Socket`
- Separate `ClientHandler` thread for each connected client
- Nickname registration and basic nickname validation
- Message broadcasting to other connected clients
- `/quit` command for disconnecting from the chat
- Console-based client and server

## Technologies

- Java
- TCP sockets
- Multithreading
- `BufferedReader` and `PrintWriter`
- `ConcurrentHashMap`

## Requirements

- JDK 8 or newer
- IntelliJ IDEA (or another Java IDE)

## Run the application

1. Clone or download this repository.
2. Open the project in IntelliJ IDEA.
3. Run `ChatServer.java` first. The server listens on port `5000`.
4. Run `ChatClient.java` in a separate application instance.
5. Start additional client instances to connect more users.
6. Enter a nickname in each client.
7. Type a message and press Enter to send it.
8. Type `/quit` to leave the chat.

The default host is `localhost`, so the server and clients are configured to run on the same computer.

## Project structure

```text
java-client-server-chat/
└── src/
    ├── ChatClient.java
    └── ChatServer.java
```

## How it works

The server creates a `ServerSocket` and waits for incoming connections. When a client connects, the server creates a `ClientHandler` and starts it on a separate thread. The client sends its nickname using the `NICK` command and sends chat messages using the `MSG` command. The server broadcasts each accepted message to the other connected clients. The `QUIT` command ends the client session.

## Limitations

This is an educational project intended to demonstrate basic client-server networking and multithreading. It uses plain TCP communication and does not include encryption, persistent message history, or production-grade authentication.
