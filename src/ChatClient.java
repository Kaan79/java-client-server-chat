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
             PrintWriter serverOutput = new PrintWriter(
                     socket.getOutputStream(), true);
             BufferedReader keyboard = new BufferedReader(
                     new InputStreamReader(System.in))) {

            System.out.println("Connected to the chat server.");
            System.out.print("Enter your nickname: ");

            String nickname = keyboard.readLine();

            if (nickname == null || nickname.trim().isEmpty()) {
                System.out.println("Nickname cannot be empty.");
                return;
            }

            serverOutput.println("NICK " + nickname);

            String response = serverInput.readLine();
            System.out.println(response);

            if (response == null || !response.startsWith("OK ")) {
                return;
            }

            Thread receiver = new Thread(() -> {
                try {
                    String message;

                    while ((message = serverInput.readLine()) != null) {
                        System.out.println("\n" + message);
                        System.out.print("> ");
                    }

                } catch (IOException e) {
                    System.out.println("Connection to server closed.");
                }
            });

            receiver.setDaemon(true);
            receiver.start();

            System.out.println("Type messages and press Enter.");
            System.out.println("Type /quit to leave the chat.");

            String message;

            while ((message = keyboard.readLine()) != null) {
                if (message.equalsIgnoreCase("/quit")) {
                    serverOutput.println("QUIT");
                    break;
                }

                serverOutput.println("MSG " + message);
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }

        System.out.println("Disconnected from chat.");
    }
}