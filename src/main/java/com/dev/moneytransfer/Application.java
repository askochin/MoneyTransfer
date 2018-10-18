package com.dev.moneytransfer;

import com.google.inject.Guice;
import com.google.inject.Inject;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class Application {

    public void run(final int port) {

        TransferHandler transferHandler = Guice.createInjector(new TransferModule("jdbc:h2:mem:accts"))
                .getInstance(TransferHandler.class);

        port(port);
        post("/transfer/:fromAccount/:toAccount", transferHandler);
    }

    public static void main(final String... args) {
        new Application().run(9999);
    }
}
