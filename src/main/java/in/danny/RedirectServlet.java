package in.danny;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RedirectServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RedirectServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String shortCode = req.getPathInfo().substring(1);
        String url = UrlStore.map.get(shortCode);
        if (url == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Shortcode not found");
            log.warn("Shortcode not found: {}", shortCode);
            return;
        }
        log.info("Redirecting shortcode [{}] -> {}", shortCode, url);
        resp.sendRedirect(url);
    }
}