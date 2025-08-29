package in.danny.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Tạo short code bằng SHA-256 rồi encode Base62
    public static String generateShortCode(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes());

            // Lấy 6 ký tự đầu tiên (có thể điều chỉnh độ dài)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                int index = (hash[i] & 0xFF) % 62;
                sb.append(BASE62.charAt(index));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
