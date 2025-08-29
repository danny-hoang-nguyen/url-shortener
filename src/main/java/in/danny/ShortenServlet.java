package in.danny;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ShortenServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ShortenServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getParameter("url");
        if (url == null || url.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing url param");
            return;
        }

        String shortCode = Integer.toHexString(url.hashCode());
        UrlStore.map.putIfAbsent(shortCode, url);

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

        log.info("Shortened URL [{}] -> {}", url, shortUrl);

        resp.setContentType("text/plain");
        resp.getWriter().write("Shortened: " + shortUrl);
    }

}
