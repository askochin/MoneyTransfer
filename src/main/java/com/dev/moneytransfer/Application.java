package com.dev.moneytransfer;

import com.google.inject.Guice;
import com.google.inject.Inject;

import static spark.Spark.port;
import static spark.Spark.post;

public class Application {

    private final TransferHandler transferHandler;

    @Inject
    Application(final TransferHandler transferHandler) {
        this.transferHandler = transferHandler;
    }

    void run(final int port) {
        port(port);

        port(port);
        post("/transfer/:fromAccount/:toAccount", transferHandler);
    }

    public static void main(final String... args) {
        Guice.createInjector(new TransferModule("jdbc:h2:mem:accts"))
                .getInstance(Application.class)
                .run(9999);
    }
}
