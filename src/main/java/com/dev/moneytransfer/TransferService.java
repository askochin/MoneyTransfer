package com.dev.moneytransfer;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TransferService {

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private final TransferDao dao;

    @Inject
    TransferService(TransferDao dao) {
        this.dao = dao;
    }

    public void transfer(String acctFrom, String acctTo, BigDecimal sum) {
        int compareResult = acctFrom.compareTo(acctTo);
        if (compareResult == 0) {
            throw new IllegalArgumentException("The same account");
        }
        boolean transferred;
        if (compareResult < 0) {
            synchronized (getLock(acctFrom)) {
                synchronized (getLock(acctTo)) {
                    transferred = dao.transfer(acctFrom, acctTo, sum);
                }
            }
        } else {
            synchronized (getLock(acctTo)) {
                synchronized (getLock(acctFrom)) {
                    transferred = dao.transfer(acctFrom, acctTo, sum);
                }
            }
        }
        if (!transferred) {
            List<String> existingAccts = dao.getExistingAccountsOf(acctFrom, acctTo);
            if (!existingAccts.contains(acctFrom)) {

            }
        }
    }

    private Object getLock(String acct) {
        return locks.putIfAbsent(acct, acct);
    }
}
