package com.dev.moneytransfer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;


@Singleton
public class TransferHandler implements Route {

    private static final Logger log = LoggerFactory.getLogger(TransferHandler.class);

    private final TransferService service;

    @Inject
    TransferHandler(TransferService service) {
        this.service = service;
    }

    @Override
    public String handle(Request request, Response response) {
        try {
            String acctFrom = request.params(":fromAccount");
            String acctTo = request.params(":toAccount");
            String sumAttr = request.queryParams("sum");
            if (sumAttr == null) {
                throw new IllegalArgumentException("Missing sum attribute");
            }

            BigDecimal sum;
            try {
                sum = new BigDecimal(sumAttr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Incorrect sum: '" + sumAttr + "'");
            }

            Long transferId = service.transfer(acctFrom, acctTo, sum);
            response.status(200);
            return transferId.toString();
        }
        catch (IllegalArgumentException ex) {
            response.status(400);
            return ex.getMessage();
        }
        catch (Exception ex) {
            log.error("Error while transfer", ex);
            response.status(500);
            return "Internal server error";
        }
    }
}

