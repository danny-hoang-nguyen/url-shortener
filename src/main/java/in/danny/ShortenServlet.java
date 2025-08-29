package in.danny;

import in.danny.exception.InvalidURLException;
import in.danny.util.HashUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Random;

import static in.danny.util.HashUtil.BASE62;

public class ShortenServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ShortenServlet.class);
    private static final Random RANDOM = new Random();
    public static final int BASE_62_LENGTH = BASE62.length();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String originalUrl = req.getParameter("url");
        if (originalUrl == null || originalUrl.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing url param");
            return;
        }

        validateUrl(originalUrl);

        // Tạo short code bằng hash
        String shortCode = HashUtil.generateShortCode(originalUrl);

        // Nếu hash bị trùng với URL khác → xử lý thêm (ở đây mình nối thêm số random)
        if (UrlStore.map.containsKey(shortCode) && !UrlStore.map.get(shortCode).equals(originalUrl)) {
            shortCode = shortCode + BASE62.charAt(RANDOM.nextInt(BASE_62_LENGTH));
        }

        UrlStore.map.put(shortCode, originalUrl);
        UrlStore.reverseMap.put(originalUrl, shortCode);


        // Lấy thông tin scheme (http/https), host, port từ request
        String scheme = req.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = req.getScheme(); // fallback
        }
        String serverName = req.getServerName(); // domain hoặc IP được gọi
        int serverPort = req.getServerPort(); // cổng

        // Nếu là port mặc định (80 hoặc 443) thì không thêm vào URL
        String portPart = "";
        if (!((scheme.equals("http") && serverPort == 80) ||
                (scheme.equals("https")))) {
            portPart = ":" + serverPort;
        }

        String baseUrl = scheme + "://" + serverName + portPart;

        String shortUrl = baseUrl + "/r/" + shortCode;

        log.info("Shortened URL [{}] -> {}", originalUrl, shortUrl);

        resp.setContentType("text/plain");
        resp.getWriter().write("Shortened: " + shortUrl);
    }

    private static void validateUrl(String originalUrl) {
        try {
            URI uri = URI.create(originalUrl);
            uri.toURL();
        } catch (MalformedURLException e) {
            throw new InvalidURLException(originalUrl);
        }
    }

}
