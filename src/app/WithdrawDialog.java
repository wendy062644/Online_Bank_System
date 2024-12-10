package app;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.BiConsumer;

import org.json.JSONObject;

class WithdrawDialog extends JDialog {

    public WithdrawDialog(JFrame parent, BiConsumer<Double, String> onWithdraw) {
        super(parent, "Withdraw Money", true);
        setSize(400, 300);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(parent);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Amount
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(amountLabel, gbc);
        gbc.gridx = 1;
        add(amountField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Buttons
        JButton withdrawButton = new JButton("Withdraw");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(withdrawButton, gbc);
        gbc.gridx = 1;
        add(cancelButton, gbc);

        // Withdraw button action
        withdrawButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String password = new String(passwordField.getPassword());
                onWithdraw.accept(amount, password);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }
}

