package com.dev.moneytransfer;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class Application {
    public static void main(String[] args) {
        port(8995);
        post("/transfer/:fromAccount/:toAccount", new TransferHandler());
    }
}
