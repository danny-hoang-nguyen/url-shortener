package in.danny.filter;

import in.danny.Main;
import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class StatsFilter implements Filter {
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
