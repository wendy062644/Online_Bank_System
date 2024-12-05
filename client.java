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
        char[] password = passwordField.getPassword();

        try {
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
                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes()).trim();
                JSONObject response = new JSONObject(responseBody);

                if (response.getBoolean("success")) {
                    JSONObject data = response.getJSONObject("data");
                    String role = data.getString("role");
                    int userId = data.getInt("id");

                    // 檢查 JSON 是否能正確解析
                    if (response.getBoolean("success")) {
                        System.out.println("Role: " + role);
                        switch (role) {
                            case "MEMBER":
                                // 查詢餘額
                                URI balanceUri = new URI("http", null, "127.0.0.1", 5000, "/member/balance",
                                        "user_id=" + userId, null);
                                URL balanceUrl = balanceUri.toURL();
                                HttpURLConnection balanceConn = (HttpURLConnection) balanceUrl.openConnection();
                                balanceConn.setRequestMethod("GET");
                                if (balanceConn.getResponseCode() == 200) {
                                    InputStream balanceStream = balanceConn.getInputStream();
                                    String balanceResponse = new String(balanceStream.readAllBytes()).trim();
                                    JSONObject balanceJson = new JSONObject(balanceResponse);
                                    double balance = balanceJson.getJSONObject("data").getDouble("balance");
                                    new MemberFrame(balance).setVisible(true);
                                } else {
                                    JOptionPane.showMessageDialog(this, "Failed to retrieve balance.");
                                }
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
                        String error = response.getString("error");
                        System.out.println("Error: " + error);
                    }

                    this.dispose();
                } else {
                    String error = response.getString("error");
                    JOptionPane.showMessageDialog(this, "Login failed: " + error);
                }
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
    public MemberFrame(double balance) {
        setTitle("Member Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel balanceLabel = new JLabel("Current Balance: $" + balance);
        balanceLabel.setBounds(50, 50, 300, 25);
        add(balanceLabel);

        JButton transferButton = new JButton("Transfer Money");
        transferButton.setBounds(50, 100, 150, 30);
        add(transferButton);

        JButton depositButton = new JButton("Deposit Money");
        depositButton.setBounds(50, 150, 150, 30);
        add(depositButton);

        JButton withdrawButton = new JButton("Withdraw Money");
        withdrawButton.setBounds(50, 200, 150, 30);
        add(withdrawButton);

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

        createUserButton.addActionListener(e -> new CreateUserDialog(this).setVisible(true));
        modifyUserInfoButton.addActionListener(e -> new ModifyUserDialog(this).setVisible(true));
    }
}

class CreateUserDialog extends JDialog {
    public CreateUserDialog(JFrame parent) {
        super(parent, "Create User", true);
        setSize(350, 300);
        setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(10, 20, 100, 25);
        add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(120, 20, 200, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 60, 100, 25);
        add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(120, 60, 200, 25);
        add(passwordField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(10, 100, 100, 25);
        add(roleLabel);

        JComboBox<String> roleBox = new JComboBox<>(new String[] { "MEMBER", "STAFF", "ADMIN" });
        roleBox.setBounds(120, 100, 200, 25);
        add(roleBox);

        JLabel balanceLabel = new JLabel("Initial Balance:");
        balanceLabel.setBounds(10, 140, 100, 25);
        add(balanceLabel);

        JTextField balanceField = new JTextField();
        balanceField.setBounds(120, 140, 200, 25);
        add(balanceField);

        JButton createButton = new JButton("Create");
        createButton.setBounds(50, 200, 100, 25);
        add(createButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(180, 200, 100, 25);
        add(cancelButton);

        createButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            String role = (String) roleBox.getSelectedItem();
            String balanceStr = balanceField.getText();

            try {
                double balance = Double.parseDouble(balanceStr); // 解析初始餘額
                URI createUserUri = new URI("http", null, "127.0.0.1", 5000, "/admin/create_user", null, null);
                URL createUserUrl = createUserUri.toURL();
                HttpURLConnection conn = (HttpURLConnection) createUserUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", new String(password));
                json.put("role", role);
                json.put("balance", balance);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                if (conn.getResponseCode() == 201) {
                    JOptionPane.showMessageDialog(this, "User created successfully!");
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(this, "Failed to create user: " + errorResponse);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid balance amount!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }
}

class ModifyUserDialog extends JDialog {
    public ModifyUserDialog(JFrame parent) {
        super(parent, "Modify User Info", true);
        setSize(300, 250);
        setLayout(null);

        JLabel userIdLabel = new JLabel("User ID:");
        userIdLabel.setBounds(10, 20, 80, 25);
        add(userIdLabel);

        JTextField userIdField = new JTextField();
        userIdField.setBounds(100, 20, 165, 25);
        add(userIdField);

        JLabel passwordLabel = new JLabel("New Password:");
        passwordLabel.setBounds(10, 60, 100, 25);
        add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(120, 60, 145, 25);
        add(passwordField);

        JLabel roleLabel = new JLabel("New Role:");
        roleLabel.setBounds(10, 100, 80, 25);
        add(roleLabel);

        JComboBox<String> roleBox = new JComboBox<>(new String[] { "MEMBER", "STAFF", "ADMIN" });
        roleBox.setBounds(100, 100, 165, 25);
        add(roleBox);

        JButton modifyButton = new JButton("Modify");
        modifyButton.setBounds(50, 150, 100, 25);
        add(modifyButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(160, 150, 100, 25);
        add(cancelButton);

        modifyButton.addActionListener(e -> {
            String userId = userIdField.getText();
            char[] password = passwordField.getPassword();
            String role = (String) roleBox.getSelectedItem();

            try {
                URI modifyUserUri = new URI("http", null, "127.0.0.1", 5000, "/admin/modify_user", null, null);
                URL modifyUserUrl = modifyUserUri.toURL();
                HttpURLConnection conn = (HttpURLConnection) modifyUserUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                json.put("password", new String(password));
                json.put("role", role);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                if (conn.getResponseCode() == 200) {
                    JOptionPane.showMessageDialog(this, "User info updated successfully!");
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(this, "Failed to update user: " + errorResponse);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }
}
