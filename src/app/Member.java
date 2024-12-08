package app;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class Member {
    private int userId;
    private String username;
    private BigDecimal balance;
    private String userPassword;

    public Member(int userId, String username, BigDecimal initialBalance, String userPassword) {
        this.userId = userId;
        this.username = username;
        this.balance = initialBalance;
        this.userPassword = userPassword;
    }

    // 💰 取款行為
    public void withdraw(BigDecimal amount) {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/withdraw", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("user_id", userId);
            json.put("amount", amount);
            json.put("password", userPassword);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "Withdrawal successful!");
                updateBalance();
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Withdrawal failed: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // 💰 存款行為
    public void deposit(BigDecimal amount) {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/deposit", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("user_id", userId);
            json.put("amount", amount);
            json.put("password", userPassword);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "Deposit successful!");
                updateBalance();
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Deposit failed: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // 💸 轉賬行為
    public void transfer(int recipientId, BigDecimal amount) {
        try {
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
            json.put("password", userPassword);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "Transfer successful!");
                updateBalance();
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Transfer failed: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // 🔄 更新餘額
    public void updateBalance() {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/member/balance", "user_id=" + userId, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes()).trim();
                JSONObject response = new JSONObject(responseBody);
                balance = response.getJSONObject("data").getBigDecimal("balance");
                System.out.println("Updated balance: $" + balance);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update balance.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // Getter & Setter
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setPassword(String newPassword) {
        this.userPassword = newPassword;
    }
}

