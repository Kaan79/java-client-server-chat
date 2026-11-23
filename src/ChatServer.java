import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;

    private static final Map<Socket, ClientHandler> clients =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server is starting...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection: " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket);
                clients.put(socket, handler);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void broadcast(String message, Socket sender) {
        for (Map.Entry<Socket, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(sender)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    private static void removeClient(Socket socket) {
        ClientHandler handler = clients.remove(socket);

        if (handler != null && handler.getNickname() != null) {
            broadcast(handler.getNickname() + " left the chat.", socket);
        }

        System.out.println("Client disconnected.");
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Socket client = socket;
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(client.getInputStream()));
                 PrintWriter writer = new PrintWriter(
                         client.getOutputStream(), true)) {

                input = reader;
                output = writer;

                String line;

                while ((line = input.readLine()) != null) {
                    if (line.startsWith("NICK ")) {
                        handleNickname(line.substring(5).trim());
                    } else if (line.startsWith("MSG ")) {
                        handleMessage(line.substring(4));
                    } else if (line.equals("QUIT")) {
                        output.println("OK Goodbye");
                        break;
                    } else {
                        output.println("ERROR Unknown command");
                    }
                }

            } catch (IOException e) {
                System.out.println("Connection ended: " + e.getMessage());
            } finally {
                removeClient(socket);
            }
        }

        private void handleNickname(String requestedNickname) {
            if (requestedNickname.isEmpty()) {
                output.println("ERROR Nickname cannot be empty");
                return;
            }

            for (ClientHandler client : clients.values()) {
                if (client != this &&
                        requestedNickname.equalsIgnoreCase(client.getNickname())) {
                    output.println("ERROR Nickname already taken");
                    return;
                }
            }

            nickname = requestedNickname;
            output.println("OK Nickname set to " + nickname);
            System.out.println(nickname + " joined the chat.");
        }

        private void handleMessage(String message) {
            if (nickname == null) {
                output.println("ERROR Set your nickname first using NICK");
                return;
            }

            if (message.trim().isEmpty()) {
                output.println("ERROR Message cannot be empty");
                return;
            }

            output.println("OK Message received");
            System.out.println(nickname + ": " + message);

            broadcast(nickname + ": " + message, socket);
        }

        public void sendMessage(String message) {
            if (output != null) {
                output.println(message);
            }
        }

        public String getNickname() {
            return nickname;
        }
    }
}