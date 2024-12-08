package app;

import javax.swing.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import org.json.JSONObject;

public class CreateUserDialog extends JDialog {
    public CreateUserDialog(JFrame parent, Runnable onSuccess) {
        super(parent, "Create User", true);
        setSize(350, 300);
        setLayout(null);
        
        // Input Fields
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleBox = new JComboBox<>(new String[] { "MEMBER", "STAFF", "ADMIN" });
        JLabel balanceLabel = new JLabel("Balance:");
        JTextField balanceField = new JTextField();
        
        // Create & Cancel Buttons
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();
            double balance = Double.parseDouble(balanceField.getText());

            AdminService.createUser(username, password, role, balance);
            onSuccess.run();
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }
}
