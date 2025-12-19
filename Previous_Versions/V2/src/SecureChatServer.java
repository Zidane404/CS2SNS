import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public class SecureChatServer {
    private static final int PORT = 12346;
    private static final String KEYSTORE_PATH = "server.keystore";
    private static final String KEYSTORE_PASSWORD = "chatserver123";

    private static Set<SecureClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static int clientCounter = 0;

    public static void main(String[] args) {
        System.out.println("=== Secure Chat Server (SSL/TLS) Started ===");
        System.out.println("Listening on port: " + PORT);
        System.out.println("All communications are encrypted\n");

        try {
            SSLServerSocketFactory sslServerSocketFactory = createSSLContext();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);

            String[] supportedCiphers = serverSocket.getSupportedCipherSuites();
            System.out.println("Enabled cipher suites: " + Arrays.toString(supportedCiphers));

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("Secure connection from: " + clientSocket.getInetAddress());
                System.out.println("Cipher suite: " + clientSocket.getSession().getCipherSuite());

                SecureClientHandler handler = new SecureClientHandler(clientSocket);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLServerSocketFactory createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        return sslContext.getServerSocketFactory();
    }

    public static void broadcastMessage(String message, SecureClientHandler sender) {
        System.out.println("[ENCRYPTED BROADCAST] " + message);
        for (SecureClientHandler client : clientHandlers) {
            client.sendMessage(message);
        }
    }

    public static void removeClient(SecureClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("Client disconnected. Active clients: " + clientHandlers.size());
    }

    static class SecureClientHandler implements Runnable {
        private SSLSocket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String username;

        public SecureClientHandler(SSLSocket socket) {
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

                System.out.println("User '" + username + "' joined the secure chat");
                broadcastMessage("SERVER: " + username + " joined the chat!", this);
                writer.println("SERVER: Welcome " + username + "! All messages are encrypted.");

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