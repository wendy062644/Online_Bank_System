import javax.swing.*;
import java.awt.*;
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 設置邊距

        // 帳號
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        // 密碼
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        // 登錄
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(Color.GREEN);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        add(panel);

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
                                    new MemberFrame(balance, userId, new String(password)).setVisible(true);
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

// 取款
class WithdrawDialog extends JDialog {
    public WithdrawDialog(JFrame parent, int userId, Runnable updateBalance) {
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
            String amountStr = amountField.getText();
            String password = new String(passwordField.getPassword());

            try {
                double amount = Double.parseDouble(amountStr);

                URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/withdraw", null, null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                json.put("amount", amount);
                json.put("password", password); // 动态输入密码

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                if (conn.getResponseCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Withdrawal successful!");
                    updateBalance.run();
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(this, "Withdrawal failed: " + errorResponse);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }

            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }
}

// 匯款
class TransferDialog extends JDialog {
    public TransferDialog(JFrame parent, int userId, Runnable updateBalance) {
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
            String recipientId = recipientField.getText();
            String amountStr = amountField.getText();
            String password = new String(passwordField.getPassword());

            try {
                double amount = Double.parseDouble(amountStr);

                URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/transfer", null, null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                json.put("recipient_id", recipientId);
                json.put("amount", amount);
                json.put("password", password); // 将密码传递给后端

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                if (conn.getResponseCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Transfer successful!");
                    updateBalance.run();
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(this, "Transfer failed: " + errorResponse);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }

            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }
}

// 存款
class DepositDialog extends JDialog {
    public DepositDialog(JFrame parent, int userId, Runnable updateBalance) {
        super(parent, "Deposit Money", true);
        setSize(400, 300);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(parent);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JButton depositButton = new JButton("Deposit");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(amountLabel, gbc);
        gbc.gridx = 1;
        add(amountField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(depositButton, gbc);
        gbc.gridx = 1;
        add(cancelButton, gbc);

        depositButton.addActionListener(e -> {
            String amountStr = amountField.getText();
            String password = new String(passwordField.getPassword());

            try {
                double amount = Double.parseDouble(amountStr);

                URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/deposit", null, null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                json.put("amount", amount);
                json.put("password", password);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                if (conn.getResponseCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Deposit successful!");
                    updateBalance.run();
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(this, "Deposit failed: " + errorResponse);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }

            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }
}

// Member Frame
class MemberFrame extends JFrame {
    private double balance;
    private int userId;
    private String userPassword;

    public MemberFrame(double initialBalance, int userId, String userPassword) {
        this.balance = initialBalance;
        this.userId = userId;
        this.userPassword = userPassword;

        setTitle("Member Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel balanceLabel = new JLabel("Current Balance: $" + balance, JLabel.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setForeground(Color.BLUE);
        panel.add(balanceLabel);

        JButton transferButton = new JButton("Transfer Money");
        JButton depositButton = new JButton("Deposit Money");
        JButton withdrawButton = new JButton("Withdraw Money");
        JButton logoutButton = new JButton("Logout");

        panel.add(transferButton);
        panel.add(depositButton);
        panel.add(withdrawButton);
        panel.add(logoutButton);

        // 更新餘額
        Runnable updateBalance = () -> {
            try {
                URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/balance", "user_id=" + userId, null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    InputStream inputStream = conn.getInputStream();
                    String responseBody = new String(inputStream.readAllBytes()).trim();
                    JSONObject response = new JSONObject(responseBody);
                    balance = response.getJSONObject("data").getDouble("balance");
                    balanceLabel.setText("Current Balance: $" + balance);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update balance.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        };

        // 按鈕事件
        transferButton.addActionListener(e -> new TransferDialog(this, userId, updateBalance).setVisible(true));
        depositButton.addActionListener(e -> new DepositDialog(this, userId, updateBalance).setVisible(true));
        withdrawButton.addActionListener(e -> new WithdrawDialog(this, userId, updateBalance).setVisible(true));
        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Logging out...");
            this.dispose();
            new client().setVisible(true); // 返回登录界面
        });

        add(panel);
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
