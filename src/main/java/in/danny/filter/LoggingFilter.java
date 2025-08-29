package in.danny.filter;

import in.danny.Main;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingFilter implements Filter {
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
