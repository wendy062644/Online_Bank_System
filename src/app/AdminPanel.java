package app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminPanel implements UserPanel {
    private JFrame frame;
    private JTable userTable;

    public AdminPanel() {
        frame = new JFrame("Admin Dashboard");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton createUserButton = new JButton("Create Account");
        topPanel.add(refreshButton);
        topPanel.add(createUserButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // User Table
        userTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton modifyUserButton = new JButton("Modify User");
        JButton logoutButton = new JButton("Logout");
        bottomPanel.add(modifyUserButton);
        bottomPanel.add(logoutButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Button Actions
        createUserButton.addActionListener(e -> new CreateUserDialog(frame, this::fetchUsers).setVisible(true));
        modifyUserButton.addActionListener(e -> modifyUser());
        logoutButton.addActionListener(e -> logout());

        fetchUsers();

        refreshButton.addActionListener(e -> fetchUsers());
    }

    private void modifyUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) userTable.getValueAt(selectedRow, 0);
            String role = (String) userTable.getValueAt(selectedRow, 3);
            String newPassword = JOptionPane.showInputDialog(frame, "Enter new password (leave blank to keep current):");

            if (newPassword == null) {
                return; // User clicked cancel
            }

            AdminService.modifyUser(userId, newPassword, role);
            fetchUsers();
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a user to modify.");
        }
    }

    private void logout() {
        frame.dispose();
        new LoginPage().setVisible(true);
    }

    private void fetchUsers() {
        AdminService.fetchUsers(userTable);
    }

    @Override
    public void showPanel() {
        frame.setVisible(true);
    }
}
