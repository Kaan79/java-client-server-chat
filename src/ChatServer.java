import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server starting on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running. Waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getRemoteSocketAddress());
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void broadcast(String message, String excludedNickname) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludedNickname)) {
                entry.getValue().send(message);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter output;
        private String nickname;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Socket clientSocket = socket;
                 BufferedReader input = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                output = writer;
                String registration = input.readLine();
                if (registration == null || !registration.startsWith("NICK ")) {
                    send("ERROR: Send NICK <nickname> first.");
                    return;
                }

                nickname = registration.substring(5).trim();
                if (nickname.isEmpty() || clients.putIfAbsent(nickname, this) != null) {
                    send("ERROR: Nickname is empty or already in use.");
                    return;
                }

                send("Welcome, " + nickname + "! Type /quit to leave.");
                broadcast("[SERVER] " + nickname + " joined the chat.", nickname);
                System.out.println(nickname + " joined.");

                String line;
                while ((line = input.readLine()) != null) {
                    if (line.equalsIgnoreCase("QUIT")) {
                        break;
                    }
                    if (line.startsWith("MSG ")) {
                        String message = line.substring(4).trim();
                        if (!message.isEmpty()) {
                            String formatted = nickname + ": " + message;
                            System.out.println(formatted);
                            broadcast(formatted, nickname);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection error for " + nickname + ": " + e.getMessage());
            } finally {
                if (nickname != null && clients.remove(nickname, this)) {
                    broadcast("[SERVER] " + nickname + " left the chat.", nickname);
                    System.out.println(nickname + " left.");
                }
            }
        }

        void send(String message) {
            PrintWriter writer = output;
            if (writer != null) {
                writer.println(message);
            }
        }
    }
}
