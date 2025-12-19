import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static int clientCounter = 0;

    public static void main(String[] args) {
        System.out.println("=== Chat Server Started ===");
        System.out.println("Listening on port: " + PORT);
        System.out.println("Waiting for clients to connect...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        System.out.println("[BROADCAST] " + message);
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(message);
        }
    }

    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("Client disconnected. Active clients: " + clientHandlers.size());
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                writer.println("SERVER: Enter your username:");
                username = reader.readLine();

                if (username == null || username.trim().isEmpty()) {
                    username = "User" + (++clientCounter);
                }

                System.out.println("User '" + username + "' joined the chat");
                broadcastMessage("SERVER: " + username + " joined the chat!", this);
                writer.println("SERVER: Welcome " + username + "! Type your messages below.");

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.trim().isEmpty()) continue;

                    if (message.equalsIgnoreCase("/exit") || message.equalsIgnoreCase("/quit")) {
                        break;
                    }

                    String formattedMessage = username + ": " + message;
                    broadcastMessage(formattedMessage, this);
                }

            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        public void sendMessage(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }

        private void cleanup() {
            try {
                if (username != null) {
                    broadcastMessage("SERVER: " + username + " left the chat.", this);
                }
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
            removeClient(this);
        }
    }
}

