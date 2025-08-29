package in.danny.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExceptionHandlingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (RuntimeException e) {
            log.error("Unhandled exception", e);
            ((HttpServletResponse) response)
                    .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unhandled exception");
        }
    }
}