package com.dev.moneytransfer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;

import static java.lang.String.valueOf;

/**
 * A Spark handler to process money transfer request.
 * Produces:
 * - status=200, body=<TransferID> - money transferred
 * - status=400, body=<ErrorMessage> - bad request
 * - status=500, body=Internal server error
 */
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
            String amountAttr = request.queryParams("amount");
            if (amountAttr == null) {
                throw new IllegalArgumentException("Missing amount param");
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountAttr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Incorrect amount: '" + amountAttr + "'");
            }

            long transferId = service.transfer(acctFrom, acctTo, amount);
            response.status(200);
            return valueOf(transferId);
        }
        catch (IllegalArgumentException ex) {
            log.warn("Illegal transfer request: " + ex.getMessage());
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

