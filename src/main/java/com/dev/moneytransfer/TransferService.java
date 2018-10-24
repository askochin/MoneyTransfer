package com.dev.moneytransfer;

import com.dev.moneytransfer.dao.AbstractTransferDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

@Singleton
public class TransferService {

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private final AbstractTransferDao dao;

    @Inject
    TransferService(AbstractTransferDao dao) {
        this.dao = dao;
    }

    public Long transfer(String acctFrom, String acctTo, BigDecimal amount) {

        requireNonNull(acctFrom, "acctFrom is null");
        requireNonNull(acctTo, "acctTo is null");
        requireNonNull(amount, "amount is null");

        amount = amount.setScale(2, HALF_UP);
        if (amount.compareTo(ZERO) <= 0) {
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

        return transferId;
    }

    private Object getLock(String acct) {
        Object lock = locks.putIfAbsent(acct, acct);
        return lock == null ? acct : lock;
    }
}
