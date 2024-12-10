package app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;

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
        createUserButton.addActionListener(e -> createUser());
        modifyUserButton.addActionListener(e -> modifyUser());
        logoutButton.addActionListener(e -> logout());

        fetchUsers();

        refreshButton.addActionListener(e -> fetchUsers());
    }

    /**
     * **新增用戶的流程**
     * - 觸發 CreateUserDialog，並在用戶創建後刷新用戶列表
     */
    private void createUser() {
        new CreateUserDialog(frame, this::fetchUsers).setVisible(true);
    }

    /**
     * **修改用戶的流程**
     * - 選擇用戶後，會提示輸入新的密碼
     * - 如果用戶取消操作則不執行後續請求
     * - 成功後，刷新用戶列表
     */
    private void modifyUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) userTable.getValueAt(selectedRow, 0);
            String role = (String) userTable.getValueAt(selectedRow, 3);
            
            // 🛠️ 取得當前的 balance
            Object balanceObject = userTable.getValueAt(selectedRow, 2);
            double balance = 0;
            
            try {
                balance = Double.parseDouble(balanceObject.toString());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid balance for the selected user.");
                return;
            }
            
            // 🛠️ 問使用者是否要更新 balance
            String newBalanceStr = JOptionPane.showInputDialog(frame, "Enter new balance (leave blank to keep current):", balance);
            if (newBalanceStr != null && !newBalanceStr.isEmpty()) {
                try {
                    balance = Double.parseDouble(newBalanceStr);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Invalid balance value.");
                    return;
                }
            }

            String newPassword = JOptionPane.showInputDialog(frame, "Enter new password (leave blank to keep current):");
            
            if (newPassword == null) {
                return; // User clicked cancel
            }

            // 🛠️ 呼叫 AdminService 的方法
            AdminService.modifyUser(userId, newPassword, role, balance);
            fetchUsers();
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a user to modify.");
        }
    }

    /**
     * **登出操作**
     * - 關閉當前視窗並返回到登錄頁面
     */
    private void logout() {
        frame.dispose();
        new LoginPage().setVisible(true);
    }

    /**
     * **獲取用戶數據**
     * - 調用 AdminService 中的 fetchUsers 方法並將用戶列表展示在表格中
     */
    private void fetchUsers() {
        AdminService.fetchUsers(userTable);
    }

    /**
     * **顯示面板**
     * - 設置 AdminPanel 視窗為可見
     */
    @Override
    public void showPanel() {
        frame.setVisible(true);
    }
}
