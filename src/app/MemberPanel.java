package app;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.awt.event.ActionEvent;
import org.json.JSONObject;

public class MemberPanel implements UserPanel {
    private JFrame frame;
    private int userId;
    private JLabel balanceLabel;

    public MemberPanel(int userId) {
        this.userId = userId;
        frame = new JFrame("Member Dashboard");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // 確保視窗位於螢幕中央

        // 創建一個面板，設置為 GridBagLayout，方便控制按鈕和標籤的佈局
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 按鈕和標籤之間的間距

        // 1️⃣ 餘額標籤
        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 24)); // 設置字體和大小
        balanceLabel.setForeground(Color.BLUE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // 標籤佔兩列
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(balanceLabel, gbc);

        // 2️⃣ 操作按鈕
        JButton depositButton = new JButton("Deposit");
        depositButton.setPreferredSize(new Dimension(200, 50));
        depositButton.addActionListener(this::showDepositDialog);
        
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setPreferredSize(new Dimension(200, 50));
        withdrawButton.addActionListener(this::showWithdrawDialog);
        
        JButton transferButton = new JButton("Transfer");
        transferButton.setPreferredSize(new Dimension(200, 50));
        transferButton.addActionListener(this::showTransferDialog);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(200, 50));
        logoutButton.addActionListener(this::logout);

        // 3️⃣ 按鈕佈局 (兩個按鈕為一排)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; // 不自動擴展大小
        panel.add(depositButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(withdrawButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(transferButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(logoutButton, gbc);

        // 加入主面板
        frame.add(panel);
        
        // 在面板加載時獲取用戶餘額
        updateBalance();
    }

    /**
     * 獲取當前用戶的餘額，並在標籤 (balanceLabel) 上顯示
     */
    private void updateBalance() {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/balance", "user_id=" + userId, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes()).trim();
                JSONObject response = new JSONObject(responseBody);
                double balance = response.getJSONObject("data").getDouble("balance");
                balanceLabel.setText("Balance: $" + balance);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to retrieve balance.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    @Override
    public void showPanel() {
        frame.setVisible(true);
    }

    /**
     * 顯示存款的對話框
     */
    private void showDepositDialog(ActionEvent e) {
        new DepositDialog(frame, (amount, password) -> {
            MemberService.deposit(userId, amount, password);
        }).setVisible(true);	
        updateBalance();
    }

    /**
     * 顯示取款的對話框
     */
    private void showWithdrawDialog(ActionEvent e) {
        new WithdrawDialog(frame, (amount, password) -> {
            MemberService.withdraw(userId, amount, password);
        }).setVisible(true);
        updateBalance();
    }

    private void showTransferDialog(ActionEvent e) {
        new TransferDialog(frame, (recipientId, amount, password) -> {
            MemberService.transfer(userId, recipientId, amount, password);
        }).setVisible(true);
        updateBalance();
    }

    /**
     * 登出操作
     */
    private void logout(ActionEvent e) {
        frame.dispose();
        new LoginPage().setVisible(true);
    }	

}







