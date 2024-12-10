package app;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;



class TransferDialog extends JDialog {

    public TransferDialog(JFrame parent, TriConsumer<Integer, Double, String> onTransfer) {
        super(parent, "Transfer Money", true);
        setSize(400, 400);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(parent);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Recipient ID
        JLabel recipientLabel = new JLabel("Recipient ID:");
        JTextField recipientField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(recipientLabel, gbc);
        gbc.gridx = 1;
        add(recipientField, gbc);

        // Amount
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(amountLabel, gbc);
        gbc.gridx = 1;
        add(amountField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Buttons
        JButton transferButton = new JButton("Transfer");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(transferButton, gbc);
        gbc.gridx = 1;
        add(cancelButton, gbc);

        // Transfer button action
        transferButton.addActionListener(e -> {
            try {
                int recipientId = Integer.parseInt(recipientField.getText());
                double amount = Double.parseDouble(amountField.getText());
                String password = new String(passwordField.getPassword());
                onTransfer.accept(recipientId, amount, password);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }
}

