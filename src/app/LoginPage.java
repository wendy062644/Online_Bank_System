package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class LoginPage extends JFrame {

    private static final long serialVersionUID = 1L; 
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPage() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        add(panel);

        loginButton.addActionListener(e -> login());

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
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

            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", new String(password));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes());
                JSONObject response = new JSONObject(responseBody);

                if (response.getBoolean("success")) {
                    JSONObject data = response.getJSONObject("data");
                    int userId = data.getInt("id");
                    String role = data.getString("role");

                    UserPanel panel;
                    switch (role) {
                        case "ADMIN":
                            panel = new AdminPanel();
                            break;
                        case "MEMBER":
                            panel = new MemberPanel(userId);
                            break;
                        default:
                            JOptionPane.showMessageDialog(this, "Role not supported.");
                            return;
                    }
                    panel.showPanel();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Login failed: " + response.getString("error"));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
