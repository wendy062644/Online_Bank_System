package app;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class Admin extends JFrame {

    private static final long serialVersionUID = 1L;

    public Admin() {
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

        createUserButton.addActionListener(e -> showCreateUserDialog());
        modifyUserInfoButton.addActionListener(e -> showModifyUserDialog());
    }

    /**
     * 顯示創建用戶的對話框
     */
    private void showCreateUserDialog() {
        JDialog dialog = new JDialog(this, "Create User", true);
        dialog.setSize(350, 300);
        dialog.setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(10, 20, 100, 25);
        dialog.add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(120, 20, 200, 25);
        dialog.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 60, 100, 25);
        dialog.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(120, 60, 200, 25);
        dialog.add(passwordField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(10, 100, 100, 25);
        dialog.add(roleLabel);

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"MEMBER", "STAFF", "ADMIN"});
        roleBox.setBounds(120, 100, 200, 25);
        dialog.add(roleBox);

        JLabel balanceLabel = new JLabel("Initial Balance:");
        balanceLabel.setBounds(10, 140, 100, 25);
        dialog.add(balanceLabel);

        JTextField balanceField = new JTextField();
        balanceField.setBounds(120, 140, 200, 25);
        dialog.add(balanceField);

        JButton createButton = new JButton("Create");
        createButton.setBounds(50, 200, 100, 25);
        dialog.add(createButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(180, 200, 100, 25);
        dialog.add(cancelButton);

        createButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            String role = (String) roleBox.getSelectedItem();
            String balanceStr = balanceField.getText();

            try {
                double balance = Double.parseDouble(balanceStr);
                URI uri = new URI("http", null, "127.0.0.1", 5000, "/admin/create_user", null, null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                    JOptionPane.showMessageDialog(dialog, "User created successfully!");
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(dialog, "Failed to create user: " + errorResponse);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 顯示修改用戶信息的對話框
     */
    private void showModifyUserDialog() {
        JDialog dialog = new JDialog(this, "Modify User Info", true);
        dialog.setSize(300, 250);
        dialog.setLayout(null);

        JLabel userIdLabel = new JLabel("User ID:");
        userIdLabel.setBounds(10, 20, 80, 25);
        dialog.add(userIdLabel);

        JTextField userIdField = new JTextField();
        userIdField.setBounds(100, 20, 165, 25);
        dialog.add(userIdField);

        JLabel passwordLabel = new JLabel("New Password:");
        passwordLabel.setBounds(10, 60, 100, 25);
        dialog.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(120, 60, 145, 25);
        dialog.add(passwordField);

        JLabel roleLabel = new JLabel("New Role:");
        roleLabel.setBounds(10, 100, 80, 25);
        dialog.add(roleLabel);

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"MEMBER", "STAFF", "ADMIN"});
        roleBox.setBounds(100, 100, 165, 25);
        dialog.add(roleBox);

        JButton modifyButton = new JButton("Modify");
        modifyButton.setBounds(50, 150, 100, 25);
        dialog.add(modifyButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(160, 150, 100, 25);
        dialog.add(cancelButton);

        modifyButton.addActionListener(e -> {
            String userId = userIdField.getText();
            char[] password = passwordField.getPassword();
            String role = (String) roleBox.getSelectedItem();

            try {
                URI uri = new URI("http", null, "127.0.0.1", 5000, "/admin/modify_user", null, null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                    JOptionPane.showMessageDialog(dialog, "User info updated successfully!");
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(dialog, "Failed to update user: " + errorResponse);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Admin().setVisible(true));
    }
}


