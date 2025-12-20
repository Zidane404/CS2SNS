import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Scanner;

public class SecureChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12346;
    private static final String TRUSTSTORE_PATH = "client.truststore";
    private static final String TRUSTSTORE_PASSWORD = "chatclient123";

    private SSLSocket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;

    public SecureChatClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            System.out.println("Connecting to secure chat server at " + SERVER_HOST + ":" + SERVER_PORT);
            SSLSocketFactory sslSocketFactory = createSSLContext();
            socket = (SSLSocket) sslSocketFactory.createSocket(SERVER_HOST, SERVER_PORT);

            System.out.println("Secure connection established!");
            System.out.println("Cipher suite: " + socket.getSession().getCipherSuite());
            System.out.println("Protocol: " + socket.getSession().getProtocol() + "\n");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            Thread receiveThread = new Thread(new MessageReceiver());
            receiveThread.start();

            sendMessages();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_HOST);
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private static SSLSocketFactory createSSLContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(TRUSTSTORE_PATH)) {
            trustStore.load(fis, TRUSTSTORE_PASSWORD.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

        return sslContext.getSocketFactory();
    }

    private void sendMessages() {
        System.out.println("\n=== Secure Chat Commands ===");
        System.out.println("/exit or /quit - Leave the chat");
        System.out.println("All messages are encrypted with TLS\n");

        while (true) {
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("/exit") || message.equalsIgnoreCase("/quit")) {
                writer.println(message);
                System.out.println("Disconnecting from secure chat...");
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
        SecureChatClient client = new SecureChatClient();
        client.start();
    }
}
