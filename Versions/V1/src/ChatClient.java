import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;

    public ChatClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            System.out.println("Connecting to chat server at " + SERVER_HOST + ":" + SERVER_PORT);
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connected successfully!\n");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            Thread receiveThread = new Thread(new MessageReceiver());
            receiveThread.start();

            sendMessages();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_HOST);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void sendMessages() {
        System.out.println("\n=== Chat Commands ===");
        System.out.println("/exit or /quit - Leave the chat");
        System.out.println("Type your message and press Enter to send\n");

        while (true) {
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("/exit") || message.equalsIgnoreCase("/quit")) {
                writer.println(message);
                System.out.println("Disconnecting from chat...");
                break;
            }

            if (!message.trim().isEmpty()) {
                writer.println(message);
            }
        }
    }

    private void cleanup() {
        try {
            if (scanner != null) scanner.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Connection lost.");
            }
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}