import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.Base64;

public class Client {
    private static final String key = "your-16-byte-key";

    public static String encrypt(String message) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
    }

    public static String decrypt(String encryptedMessage) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)));
    }

    public static void main(String[] args) throws Exception {
        // Receive encrypted message from server
        URL url = new URL("http://localhost:5000/send_to_client");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        String encryptedMessage = new String(conn.getInputStream().readAllBytes());
        System.out.println("Decrypted message from server: " + decrypt(encryptedMessage));

        // Send encrypted message to server
        String message = "Hello Server!";
        String encrypted = encrypt(message);

        url = new URL("http://localhost:5000/receive_from_client");
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(encrypted.getBytes());
        os.flush();
        System.out.println("Server response: " + new String(conn.getInputStream().readAllBytes()));
    }
}
