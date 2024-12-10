package app;

import javax.swing.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class MemberService {

    /**
     * 存款方法，將金額存入指定的用戶帳戶中
     * 
     * @param userId 用戶ID
     * @param amount 存款金額
     * @param password 用戶的密碼
     */
    public static void deposit(int userId, double amount, String password) {
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
            json.put("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "Deposit successful!");
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Deposit failed: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /**
     * 取款方法，從用戶的帳戶中扣除金額
     * 
     * @param userId 用戶ID
     * @param amount 取款金額
     * @param password 用戶的密碼
     */
    public static void withdraw(int userId, double amount, String password) {
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
            json.put("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "Withdrawal successful!");
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Withdrawal failed: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /**
     * 轉帳方法，將金額從當前用戶帳戶轉入其他用戶帳戶
     * 
     * @param userId 發送方的用戶ID
     * @param recipientId 接收方的用戶ID
     * @param amount 轉帳金額
     * @param password 用戶的密碼
     */
    public static void transfer(int userId, int recipientId, double amount, String password) {
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
            json.put("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "Transfer successful!");
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Transfer failed: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }
}
