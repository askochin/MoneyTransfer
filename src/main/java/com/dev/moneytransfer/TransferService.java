package com.dev.moneytransfer;

import com.dev.moneytransfer.dao.TransferDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * A service to safely transfer money between 2 accounts.
 * The implementation of the service prevents deadlocks and transaction races
 * in the backing datastore by locking access to affected accounts.
 */
@Singleton
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private final TransferDao dao;

    @Inject
    TransferService(TransferDao dao) {
        this.dao = dao;
    }

    public Long transfer(String acctFrom, String acctTo, BigDecimal amount) {

        requireNonNull(acctFrom, "acctFrom");
        requireNonNull(acctTo, "acctTo");
        requireNonNull(amount, "amount");

        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Negative or zero amount");
        }

        int compareResult = acctFrom.compareTo(acctTo);
        if (compareResult == 0) {
            throw new IllegalArgumentException("Equal accounts");
        }

        long transferId;
        if (compareResult < 0) {
            synchronized (getLock(acctFrom)) {
                synchronized (getLock(acctTo)) {
                    transferId = dao.transfer(acctFrom, acctTo, amount);
                }
            }
        } else {
            synchronized (getLock(acctTo)) {
                synchronized (getLock(acctFrom)) {
                    transferId = dao.transfer(acctFrom, acctTo, amount);
                }
            }
        }
        log.info("Transfer {} of {} from {} to {}", transferId, amount, acctFrom, acctTo);
        return transferId;
    }

    private Object getLock(String acct) {
        return locks.computeIfAbsent(acct, a -> new Object());
    }
}
