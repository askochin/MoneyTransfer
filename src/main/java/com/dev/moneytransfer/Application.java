package com.dev.moneytransfer;

import com.google.inject.Guice;
import com.google.inject.Inject;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class Application {

    public void run() {

        int port = parseInt(getProperty("port"));
        String jdbcUrl = getProperty("jdbc.url");
        String dbInitScript = getProperty("db.init.script");
        int maxConnectionPoolSize = parseInt(getProperty("connection.pool.max.size"));

        // module initialization
        TransferModule module = new TransferModule(jdbcUrl, maxConnectionPoolSize, dbInitScript);
        TransferHandler transferHandler = Guice.createInjector(module).getInstance(TransferHandler.class);

        // Spark definitions
        port(port);
        post("/transfer/:fromAccount/:toAccount", transferHandler);
    }

    public static void main(final String... args) {
        new Application().run();
    }
}
