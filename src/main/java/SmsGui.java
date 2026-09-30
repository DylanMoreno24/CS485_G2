import javax.swing.*;
import java.awt.*;

public class SmsGui extends JFrame {
    private final JTextField numberField = new JTextField();
    private final JTextArea messageArea = new JTextArea(6, 30);
    private final JButton sendButton = new JButton("Send");
    private final JLabel statusLabel = new JLabel(" ");
    private SmsService service; // created on first send

    public SmsGui() {
        super("SMS Sender");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;

        c.gridy = 0;
        form.add(new JLabel("Phone number (e.g. 671 487 0509):"), c);
        c.gridy = 1;
        form.add(numberField, c);

        c.gridy = 2;
        form.add(new JLabel("Message:"), c);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        form.add(new JScrollPane(messageArea), c);

        c.gridy = 4;
        c.fill = GridBagConstraints.NONE;
        c.weighty = 0;
        c.anchor = GridBagConstraints.EAST;
        form.add(sendButton, c);

        c.gridy = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        form.add(statusLabel, c);

        add(form);
        sendButton.addActionListener(e -> onSend());
        getRootPane().setDefaultButton(sendButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void onSend() {
        String to = numberField.getText().replaceAll("\\D", ""); // digits only
        if (to.length() == 10) to = "1" + to;                    // assume US/Guam (+1)
        String text = messageArea.getText().trim();

        if (to.length() < 11) {
            setStatus("Enter a valid phone number.", true);
            return;
        }
        if (text.isEmpty()) {
            setStatus("Enter a message.", true);
            return;
        }

        sendButton.setEnabled(false);
        setStatus("Sending...", false);
        final String number = to;

        // Run the network call off the UI thread so the window doesn't freeze
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                if (service == null) service = new SmsService();
                return service.send(number, text);
            }

            @Override
            protected void done() {
                try {
                    String id = get();
                    setStatus("Sent! ID: " + id, false);
                    messageArea.setText("");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    setStatus("Failed: " + cause.getMessage(), true);
                } finally {
                    sendButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setForeground(error ? new Color(0xB00020) : Color.DARK_GRAY);
        statusLabel.setText("<html>" + msg + "</html>");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmsGui().setVisible(true));
    }
}
