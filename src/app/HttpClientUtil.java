package app;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HttpClientUtil {

    /**
     *  發送 POST 請求
     *  @param urlString  目標URL
     *  @param jsonInput  請求中的 JSON 資料
     *  @return 伺服器的回應內容
     */
    public static String sendPostRequest(String urlString, String jsonInput) throws Exception {
        // ✅ 使用 URI 替代 URL
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        // 發送數據
        conn.getOutputStream().write(jsonInput.getBytes());

        try (InputStream inputStream = conn.getInputStream()) {
            return new String(inputStream.readAllBytes());
        }
    }

    /**
     *  發送 GET 請求
     *  @param urlString  目標URL
     *  @return 伺服器的回應內容
     */
    public static String sendGetRequest(String urlString) throws Exception {
        // ✅ 使用 URI 替代 URL
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStream inputStream = conn.getInputStream()) {
            return new String(inputStream.readAllBytes());
        }
    }
}


