import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class client extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel balanceLabel;
    private JPanel loginPanel, dashboardPanel;
    private String currentUser;

    public client() {
        setTitle("Bank Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        // Login Panel
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2, 10, 10));
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginAction());
        loginPanel.add(new JLabel());
        loginPanel.add(loginButton);

        // Dashboard Panel
        dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        balanceLabel = new JLabel("Balance: $0.00", SwingConstants.CENTER);
        dashboardPanel.add(balanceLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        JButton checkBalanceButton = new JButton("Check Balance");
        checkBalanceButton.addActionListener(new CheckBalanceAction());
        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(new DepositAction());
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(new WithdrawAction());
        buttonPanel.add(checkBalanceButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        dashboardPanel.add(buttonPanel, BorderLayout.CENTER);

        add(loginPanel, "Login");
        add(dashboardPanel, "Dashboard");

        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Login");
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            try {
                String response = sendPostRequest("http://localhost:5000/login", 
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
                if (response.contains("Login successful")) {
                    currentUser = username;
                    balanceLabel.setText("Balance: $" + response.split("\"balance\":")[1].replace("}", ""));
                    ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Dashboard");
                } else {
                    JOptionPane.showMessageDialog(null, "Login failed. Please check your credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error connecting to the server.");
            }
        }
    }

    private class CheckBalanceAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, balanceLabel.getText(), "Balance", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class DepositAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String amountStr = JOptionPane.showInputDialog("Enter deposit amount:");
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    String response = sendPostRequest("http://localhost:5000/deposit", 
                            "{\"username\":\"" + currentUser + "\",\"amount\":" + amount + "}");
                    if (response.contains("Deposit successful")) {
                        balanceLabel.setText("Balance: $" + response.split("\"balance\":")[1].replace("}", ""));
                        JOptionPane.showMessageDialog(null, "Deposit successful!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Deposit failed.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid amount.");
                }
            }
        }
    }

    private class WithdrawAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String amountStr = JOptionPane.showInputDialog("Enter withdrawal amount:");
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    String response = sendPostRequest("http://localhost:5000/withdraw", 
                            "{\"username\":\"" + currentUser + "\",\"amount\":" + amount + "}");
                    if (response.contains("Withdrawal successful")) {
                        balanceLabel.setText("Balance: $" + response.split("\"balance\":")[1].replace("}", ""));
                        JOptionPane.showMessageDialog(null, "Withdrawal successful!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Insufficient funds.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid amount.");
                }
            }
        }
    }

    private String sendPostRequest(String urlString, String payload) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
        return new String(conn.getInputStream().readAllBytes());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new client().setVisible(true));
    }
}
