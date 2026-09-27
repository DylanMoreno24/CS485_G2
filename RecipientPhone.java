import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * RecipientPhone
 * ---------------
 * Simulates a single person's phone. Run one instance of this per
 * recipient, each on its own port. It listens for incoming socket
 * connections from MessageSender and displays each message it
 * receives, like a notification popping up on a phone screen.
 *
 * Usage:
 *   java RecipientPhone <port> <displayName>
 *   e.g. java RecipientPhone 5001 "Austin's Phone"
 */
public class RecipientPhone {

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5001;
        String name = args.length > 1 ? args[1] : "Recipient Phone";

        SwingUtilities.invokeLater(() -> new RecipientPhone().buildGui(port, name));
    }

    private JTextArea messageArea;

    private void buildGui(int port, String name) {
        JFrame frame = new JFrame(name + " (port " + port + ")");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320, 500);
        frame.setLayout(new BorderLayout());

        JLabel title = new JLabel(name, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(title, BorderLayout.NORTH);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        startServer(port);
    }

    /** Listens for incoming connections in a background thread so the GUI stays responsive. */
    private void startServer(int port) {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                appendMessage("[System] Listening on port " + port + "...\n");
                while (true) {
                    Socket client = serverSocket.accept();
                    handleClient(client);
                }
            } catch (IOException e) {
                appendMessage("[System] Server error: " + e.getMessage() + "\n");
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /** Reads one message from a connected sender and displays it. */
    private void handleClient(Socket client) {
        Thread clientThread = new Thread(() -> {
            try (
                Socket socket = client;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
            ) {
                // Simple protocol: first line = sender's phone number/label, remaining lines = message body,
                // terminated by a line containing only "END".
                String from = in.readLine();
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    body.append(line).append("\n");
                }
                appendMessage("\n--- New message from " + from + " ---\n" + body);
            } catch (IOException e) {
                appendMessage("[System] Error reading message: " + e.getMessage() + "\n");
            }
        });
        clientThread.start();
    }

    private void appendMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(text);
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
} 
