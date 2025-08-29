package in.danny;

import in.danny.filter.ExceptionHandlingFilter;
import in.danny.filter.LoggingFilter;
import in.danny.filter.StatsFilter;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
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
                        Servlets.filter("StatsFilter", StatsFilter.class),
                        Servlets.filter("ExceptionHandlingFilter", ExceptionHandlingFilter.class)
                )
                .addFilterUrlMapping("LoggingFilter", "/*", DispatcherType.REQUEST)
                .addFilterUrlMapping("ExceptionHandlingFilter", "/*", DispatcherType.REQUEST)
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
}