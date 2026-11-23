import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader serverInput = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader keyboard = new BufferedReader(
                     new InputStreamReader(System.in))) {

            System.out.println("Connected to the chat server.");
            System.out.print("Enter your nickname: ");
            String nickname = keyboard.readLine();

            if (nickname == null || nickname.trim().isEmpty()) {
                System.out.println("Nickname cannot be empty.");
                return;
            }

            serverOutput.println("NICK " + nickname.trim());
            String response = serverInput.readLine();
            if (response == null) {
                System.out.println("Server closed the connection.");
                return;
            }
            System.out.println(response);

            Thread receiver = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverInput.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            receiver.setDaemon(true);
            receiver.start();

            String message;
            while ((message = keyboard.readLine()) != null) {
                if (message.equalsIgnoreCase("/quit")) {
                    serverOutput.println("QUIT");
                    break;
                }
                if (!message.trim().isEmpty()) {
                    serverOutput.println("MSG " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not connect to the chat server: " + e.getMessage());
        }
    }
}
