package com.dev.moneytransfer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

@Singleton
public class TransferService {

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private final TransferDao dao;

    @Inject
    TransferService(TransferDao dao) {
        this.dao = dao;
    }

    public Long transfer(String acctFrom, String acctTo, BigDecimal sum) {

        requireNonNull(acctFrom, "acctFrom");
        requireNonNull(acctTo, "acctTo");
        requireNonNull(sum, "sum");

        sum = sum.setScale(2, RoundingMode.HALF_UP);
        if (sum.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Negative or zero sum");
        }

        int compareResult = acctFrom.compareTo(acctTo);
        if (compareResult == 0) {
            throw new IllegalArgumentException("Equal accounts");
        }

        Long transferId;
        if (compareResult < 0) {
            synchronized (getLock(acctFrom)) {
                synchronized (getLock(acctTo)) {
                    transferId = dao.transfer(acctFrom, acctTo, sum);
                }
            }
        } else {
            synchronized (getLock(acctTo)) {
                synchronized (getLock(acctFrom)) {
                    transferId = dao.transfer(acctFrom, acctTo, sum);
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
