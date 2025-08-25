package in.danny;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Map<String, String> urlStore = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(Main.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("urlshortener")
                .addServlets(
                        Servlets.servlet("ShortenServlet", ShortenServlet.class)
                                .addMapping("/shorten"),
                        Servlets.servlet("RedirectServlet", RedirectServlet.class)
                                .addMapping("/r/*")
                )
                .addFilters(
                        Servlets.filter("LoggingFilter", LoggingFilter.class),
                        Servlets.filter("StatsFilter", StatsFilter.class)
                )
                .addFilterUrlMapping("LoggingFilter", "/*", DispatcherType.REQUEST)
                .addFilterUrlMapping("StatsFilter", "/*", DispatcherType.REQUEST);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();

        PathHandler path = io.undertow.Handlers.path(manager.start());
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(path)
                .build();
        server.start();

        logger.info("🚀 URL Shortener server started on http://localhost:8080");
    }

    @WebServlet("/shorten")
    public static class ShortenServlet extends HttpServlet {
        private static final Logger log = LoggerFactory.getLogger(ShortenServlet.class);

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String url = req.getParameter("url");
            if (url == null || url.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing url param");
                return;
            }

            String shortCode = Integer.toHexString(url.hashCode());
            urlStore.putIfAbsent(shortCode, url);

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

    @WebServlet("/r/*")
    public static class RedirectServlet extends HttpServlet {
        private static final Logger log = LoggerFactory.getLogger(RedirectServlet.class);

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String shortCode = req.getPathInfo().substring(1);
            String url = urlStore.get(shortCode);
            if (url == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Shortcode not found");
                log.warn("Shortcode not found: {}", shortCode);
                return;
            }
            log.info("Redirecting shortcode [{}] -> {}", shortCode, url);
            resp.sendRedirect(url);
        }
    }

    @WebFilter("/*")
    public static class LoggingFilter implements Filter {
        private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            long start = System.currentTimeMillis();
            log.debug("👉 {} {} from {}", req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - start;
            log.debug("✅ {} {} completed in {} ms", req.getMethod(), req.getRequestURI(), duration);
        }
    }

    @WebFilter("/*")
    public static class StatsFilter implements Filter {
        private static final AtomicLong counter = new AtomicLong(0);
        private static final Logger log = LoggerFactory.getLogger(StatsFilter.class);

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            long total = counter.incrementAndGet();
            chain.doFilter(request, response);

            if (total % 10 == 0) {
                log.info("📊 Total requests so far: {}", total);
            }
        }
    }
}