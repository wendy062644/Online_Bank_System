import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class client extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public client() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(10, 30, 80, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 30, 165, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 70, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 70, 165, 25);
        add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 110, 120, 25);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
    }

    private void login() {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword(); // 使用 char[] 代替 String

        try {
            // 使用 URI 類來構建 URL
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/login", null, null);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 構建 JSON 請求
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", new String(password)); // 在這裡暫時轉換為 String，發送後立即清除密碼陣列

            // 清空密碼陣列內容以提高安全性
            java.util.Arrays.fill(password, '\0');

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() == 200) {
                JSONObject response = new JSONObject(new String(conn.getInputStream().readAllBytes()));
                String role = response.getString("role");

                switch (role) {
                    case "MEMBER":
                        new MemberFrame().setVisible(true);
                        break;
                    case "STAFF":
                        new StaffFrame().setVisible(true);
                        break;
                    case "ADMIN":
                        new AdminFrame().setVisible(true);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Unknown role!");
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new client().setVisible(true));
    }
}

// Member Frame
class MemberFrame extends JFrame {
    public MemberFrame() {
        setTitle("Member Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JButton viewBalanceButton = new JButton("View Balance");
        viewBalanceButton.setBounds(50, 50, 150, 30);
        add(viewBalanceButton);

        JButton transferButton = new JButton("Transfer Money");
        transferButton.setBounds(50, 100, 150, 30);
        add(transferButton);

        JButton depositButton = new JButton("Deposit Money");
        depositButton.setBounds(50, 150, 150, 30);
        add(depositButton);

        JButton withdrawButton = new JButton("Withdraw Money");
        withdrawButton.setBounds(50, 200, 150, 30);
        add(withdrawButton);

        viewBalanceButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Balance: $1000.00"));
        transferButton
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Transfer functionality coming soon."));
        depositButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Deposit functionality coming soon."));
        withdrawButton
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Withdraw functionality coming soon."));
    }
}

// Staff Frame
class StaffFrame extends JFrame {
    public StaffFrame() {
        setTitle("Staff Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JButton viewMemberBalanceButton = new JButton("View Member Balance");
        viewMemberBalanceButton.setBounds(50, 50, 200, 30);
        add(viewMemberBalanceButton);

        JButton viewTransactionHistoryButton = new JButton("View Transaction History");
        viewTransactionHistoryButton.setBounds(50, 100, 200, 30);
        add(viewTransactionHistoryButton);

        viewMemberBalanceButton
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Viewing member balance..."));
        viewTransactionHistoryButton
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Viewing transaction history..."));
    }
}

// Admin Frame
class AdminFrame extends JFrame {
    public AdminFrame() {
        setTitle("Admin Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JButton createUserButton = new JButton("Create User");
        createUserButton.setBounds(50, 50, 150, 30);
        add(createUserButton);

        JButton modifyUserInfoButton = new JButton("Modify User Info");
        modifyUserInfoButton.setBounds(50, 100, 150, 30);
        add(modifyUserInfoButton);

        createUserButton
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Create user functionality coming soon."));
        modifyUserInfoButton.addActionListener(
                e -> JOptionPane.showMessageDialog(this, "Modify user info functionality coming soon."));
    }
}
