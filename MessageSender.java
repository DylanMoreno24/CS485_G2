import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * MessageSender
 * -------------
 * The "Message Notification System" GUI from the assignment. The user
 * types a message, enters (or picks) a recipient phone number, and hits
 * Send. This opens a socket to the matching RecipientPhone instance and
 * forwards the message.
 *
 * Directory of phone number -> (host, port) is hardcoded below for the
 * demo. In a real deployment this could be read from a config file or
 * database instead.
 */
public class MessageSender extends JFrame {

    // Phone number -> [host, port]. Point these at whichever machines/ports
    // your RecipientPhone instances are running on. "localhost" works when
    // demoing everything on one computer.
    private static final Map<String, String[]> DIRECTORY = new LinkedHashMap<>();
    static {
        DIRECTORY.put("671-111-1111", new String[]{"localhost", "5001"});
        DIRECTORY.put("671-222-2222", new String[]{"localhost", "5002"});
    }

    private JTextArea messageInput;
    private JComboBox<String> phoneSelector;
    private JLabel statusLabel;

    public MessageSender() {
        super("Message Notification System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 420);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Message Notification System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        center.add(new JLabel("Send to:"), BorderLayout.NORTH);
        phoneSelector = new JComboBox<>(DIRECTORY.keySet().toArray(new String[0]));
        JPanel toPanel = new JPanel(new BorderLayout());
        toPanel.add(new JLabel("Send to: "), BorderLayout.WEST);
        toPanel.add(phoneSelector, BorderLayout.CENTER);
        center.add(toPanel, BorderLayout.NORTH);

        JLabel inputLabel = new JLabel("Message Input:");
        messageInput = new JTextArea("Typhoon is coming.\nGuam in COR 2 from 10:00am.\nPlease remain indoor.");
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(messageInput), BorderLayout.CENTER);
        center.add(inputPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout());
        JButton sendButton = new JButton("Send");
        JButton exitButton = new JButton("Exit");
        buttons.add(sendButton);
        buttons.add(exitButton);
        bottom.add(buttons, BorderLayout.NORTH);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        exitButton.addActionListener(e -> System.exit(0));

        setLocationRelativeTo(null);
    }

    private void sendMessage() {
        String phoneNumber = (String) phoneSelector.getSelectedItem();
        String[] target = DIRECTORY.get(phoneNumber);
        String message = messageInput.getText();

        if (target == null || message.isBlank()) {
            statusLabel.setText("Enter a message and choose a recipient.");
            return;
        }

        // Run the network call off the Event Dispatch Thread so the GUI doesn't freeze.
        new Thread(() -> {
            String host = target[0];
            int port = Integer.parseInt(target[1]);

            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("Sender (" + phoneNumber + ")"); // "from" line
                for (String line : message.split("\n")) {
                    out.println(line);
                }
                out.println("END");

                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Message sent to " + phoneNumber));

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Failed to reach " + phoneNumber + ": " + ex.getMessage()));
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MessageSender().setVisible(true));
    }
}