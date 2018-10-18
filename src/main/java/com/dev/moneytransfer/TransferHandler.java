package com.dev.moneytransfer;

import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class TransferHandler implements Route {

    @Override
    public Object handle(Request request, Response response) {

        try {
            return "!!!!!!!";
        } catch (IllegalArgumentException ex) {
            response.status(HttpStatus.BAD_REQUEST_400);
            ex.printStackTrace();
            return ex.getMessage();
        }
    }
}

