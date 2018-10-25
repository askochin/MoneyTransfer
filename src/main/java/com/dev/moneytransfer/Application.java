package com.dev.moneytransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.Guice.createInjector;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static spark.Spark.*;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public void run() {

        // application properties
        int port;
        try {
            port = parseInt(getProperty("port"));
        } catch (Exception ex) {
            log.error("Failed set server port: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return;
        }
        String jdbcUrl = getProperty("jdbc.url");
        if (jdbcUrl == null) {
            log.error("jdbc.url system property is not set");
            return;
        }
        String dbInitScript = getProperty("db.init.script");

        // module initialization
        TransferModule module = new TransferModule(jdbcUrl, dbInitScript);
        TransferHandler transferHandler = createInjector(module).getInstance(TransferHandler.class);

        // spark settings
        port(port);
        before( (request, response) -> response.type("text/plain"));
        notFound( (request, response) -> "404 Resource not found");
        internalServerError( (request, response) -> "Internal server error");

        // spark routes
        post("/transfer/:fromAccount/:toAccount", transferHandler);
    }

    public static void main(final String... args) {
        new Application().run();
    }
}
