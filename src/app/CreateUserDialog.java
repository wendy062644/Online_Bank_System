package app;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import org.json.JSONObject;

public class CreateUserDialog extends JDialog {
    public CreateUserDialog(JFrame parent, Runnable onSuccess) {
        super(parent, "Create User", true);
        setSize(400, 350);
        setLocationRelativeTo(parent); // 中央對齊父窗口
        setLayout(new GridBagLayout()); // 使用 GridBagLayout

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 控制元件之間的邊距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1️⃣ Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(usernameField, gbc);

        // 2️⃣ Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(passwordField, gbc);

        // 3️⃣ Role
        JLabel roleLabel = new JLabel("Role:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(roleLabel, gbc);

        JComboBox<String> roleBox = new JComboBox<>(new String[] { "MEMBER", "STAFF", "ADMIN" });
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(roleBox, gbc);

        // 4️⃣ Balance
        JLabel balanceLabel = new JLabel("Balance:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(balanceLabel, gbc);

        JTextField balanceField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(balanceField, gbc);

        // 5️⃣ Buttons (Create and Cancel)
        JButton createButton = new JButton("Create");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1; // 確保按鈕只佔用 1 個列
        add(createButton, gbc);

        JButton cancelButton = new JButton("Cancel");
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(cancelButton, gbc);

        // 6️⃣ Button Action
        createButton.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleBox.getSelectedItem();
                double balance = Double.parseDouble(balanceField.getText());

                if (username.isEmpty() || password.isEmpty() || role.isEmpty() || balance < 0) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields correctly!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AdminService.createUser(username, password, role, balance);
                onSuccess.run();
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Balance must be a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }
}
