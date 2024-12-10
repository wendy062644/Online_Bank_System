package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class AdminService {

    /**
     * 透過API獲取用戶列表，並將其顯示在傳入的JTable中
     * 
     * @param userTable 目標 JTable，用於顯示用戶資訊
     */
    public static void fetchUsers(JTable userTable) {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/admin/get_users", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                String responseBody = new String(inputStream.readAllBytes());
                JSONObject response = new JSONObject(responseBody);

                if (response.getBoolean("success")) {
                    JSONArray users = response.getJSONArray("data");
                    Object[][] rowData = new Object[users.length()][4];
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        rowData[i][0] = user.getInt("id");
                        rowData[i][1] = user.getString("username");
                        rowData[i][2] = user.getDouble("balance");
                        rowData[i][3] = user.getString("role");
                    }
                    userTable.setModel(new DefaultTableModel(
                            rowData,
                            new String[] { "ID", "Username", "Balance", "Role" }
                    ));
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to fetch users: " + response.getString("error"));
                }
            } else {
                JOptionPane.showMessageDialog(null, "Server error: " + conn.getResponseCode());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /**
     * 創建新用戶的請求
     * 
     * @param username 用戶名
     * @param password 用戶密碼
     * @param role     用戶的角色（如 MEMBER, STAFF, ADMIN）
     * @param balance  用戶的初始餘額
     */
    public static void createUser(String username, String password, String role, double balance) {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/admin/create_user", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);
            json.put("role", role);
            json.put("balance", balance);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 201) {
                JOptionPane.showMessageDialog(null, "User created successfully!");
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Failed to create user: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /**
     * 修改現有用戶的資訊
     * 
     * @param userId   用戶的ID
     * @param password 新的用戶密碼
     * @param role     新的角色（如 MEMBER, STAFF, ADMIN）
     */
    /**
     * 修改現有用戶的資訊
     * 
     * @param userId   用戶的ID
     * @param password 新的用戶密碼
     * @param role     新的角色（如 MEMBER, STAFF, ADMIN）
     * @param balance  新的餘額
     */
    public static void modifyUser(int userId, String password, String role, double balance) {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 5000, "/admin/modify_user", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            
            // 🛠️ 確保所有必需的欄位都不為null
            if (userId > 0) {
                json.put("user_id", userId);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid user ID.");
                return;
            }

            // 🛠️ 傳遞密碼（如果不為空）
            if (password != null && !password.isEmpty()) {
                json.put("password", password);
            }

            // 🛠️ 傳遞角色（不能為空）
            if (role != null && !role.isEmpty()) {
                json.put("role", role);
            } else {
                JOptionPane.showMessageDialog(null, "Role is required.");
                return;
            }

            // 🛠️ 傳遞 balance，如果 balance < 0，報錯
            if (balance >= 0) {
                json.put("balance", balance);
            } else {
                JOptionPane.showMessageDialog(null, "Balance cannot be negative.");
                return;
            }

            // 🛠️ 發送請求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(null, "User modified successfully!");
            } else {
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = new String(errorStream.readAllBytes());
                JOptionPane.showMessageDialog(null, "Failed to modify user: " + errorResponse);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

}
