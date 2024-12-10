package app;

import javax.swing.*;

public class DepositDialog extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;

    public DepositDialog(JFrame parent, DepositCallback callback) {
        super(parent, "Deposit Money", true);
        setSize(350, 250);
        setLayout(null);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(10, 20, 100, 25);
        add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(120, 20, 200, 25);
        add(amountField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 60, 100, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 60, 200, 25);
        add(passwordField);

        JButton depositButton = new JButton("Deposit");
        depositButton.setBounds(50, 150, 100, 25);
        add(depositButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(180, 150, 100, 25);
        add(cancelButton);

        depositButton.addActionListener(e -> {
            double amount = Double.parseDouble(amountField.getText());
            String password = new String(passwordField.getPassword());
            callback.onDeposit(amount, password);
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    public interface DepositCallback {
        void onDeposit(double amount, String password);
    }
}
