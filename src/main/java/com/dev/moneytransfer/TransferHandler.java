package com.dev.moneytransfer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;


@Singleton
public class TransferHandler implements Route {

    private final TransferService service;

    @Inject
    TransferHandler(TransferService service) {
        this.service = service;
    }

    @Override
    public String handle(Request request, Response response) {
        String acctFrom = request.params(":fromAccount");
        String acctTo = request.params(":fromTo");
        BigDecimal sum = BigDecimal.valueOf(request.attribute("sum"));
        service.transfer(acctFrom, acctTo, sum);
        response.status(HttpStatus.OK_200);
        return "";
    }
}

