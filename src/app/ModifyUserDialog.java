package app;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class ModifyUserDialog extends JDialog {

    public ModifyUserDialog(JFrame parent, int userId, String currentUsername, String currentRole, Runnable onUpdate) {
        super(parent, "Modify User Info", true);
        setSize(350, 300);
        setLayout(null);

        // 顯示用戶 ID
        JLabel userIdLabel = new JLabel("User ID: " + userId);
        userIdLabel.setBounds(10, 20, 200, 25);
        add(userIdLabel);

        // 顯示用戶名
        JLabel usernameLabel = new JLabel("Username: " + currentUsername);
        usernameLabel.setBounds(10, 50, 200, 25);
        add(usernameLabel);

        // 修改密碼
        JLabel passwordLabel = new JLabel("New Password:");
        passwordLabel.setBounds(10, 90, 100, 25);
        add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(120, 90, 200, 25);
        add(passwordField);

        // 修改角色
        JLabel roleLabel = new JLabel("New Role:");
        roleLabel.setBounds(10, 130, 80, 25);
        add(roleLabel);

        JComboBox<String> roleBox = new JComboBox<>(new String[] { "MEMBER", "STAFF", "ADMIN" });
        roleBox.setBounds(120, 130, 200, 25);
        roleBox.setSelectedItem(currentRole); // 預設選擇當前角色
        add(roleBox);

        // 修改按鈕
        JButton modifyButton = new JButton("Modify");
        modifyButton.setBounds(50, 200, 100, 25);
        add(modifyButton);

        // 取消按鈕
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(180, 200, 100, 25);
        add(cancelButton);

        // **按鈕行為 - 修改按鈕**
        modifyButton.addActionListener(e -> {
            char[] password = passwordField.getPassword();
            String role = (String) roleBox.getSelectedItem();

            try {
                if (password.length == 0 && role.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a new password or select a new role.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 構建 API 請求
                URI modifyUserUri = new URI("http", null, "127.0.0.1", 5000, "/admin/modify_user", null, null);
                URL modifyUserUrl = modifyUserUri.toURL();
                HttpURLConnection conn = (HttpURLConnection) modifyUserUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // **建立 JSON 請求**
                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                if (password.length > 0) {
                    json.put("password", new String(password)); // 更新密碼
                }
                json.put("role", role); // 更新角色

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                // **處理伺服器回應**
                if (conn.getResponseCode() == 200) {
                    JOptionPane.showMessageDialog(this, "User info updated successfully!");
                    onUpdate.run(); // 觸發更新回呼
                    dispose(); // 關閉對話框
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = new String(errorStream.readAllBytes());
                    JOptionPane.showMessageDialog(this, "Failed to update user: " + errorResponse, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // **按鈕行為 - 取消按鈕**
        cancelButton.addActionListener(e -> dispose());
    }
}
