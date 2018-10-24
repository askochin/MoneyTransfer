package com.dev.moneytransfer.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.math.BigDecimal;

import static org.jdbi.v3.core.transaction.TransactionIsolationLevel.READ_COMMITTED;

@Singleton
public class DeadlockSafeTransferDao extends AbstractTransferDao {

    @Inject
    DeadlockSafeTransferDao(Jdbi jdbi) {
        super(jdbi);
    }

    /**
     * Transfer amount of money from acctFrom to acctTo.
     * Account records are being update is alphabetical order to prevent transaction deadlock.
     * @return The ID of transfer
     * @throws IllegalArgumentException if any account is not found or there's not sufficient funds at acctFrom
     */
    public long transfer(String acctFrom, String acctTo, BigDecimal amount) {

        return jdbi().inTransaction(READ_COMMITTED, handle -> {

            if (acctFrom.compareTo(acctTo) < 0) {
                deductFromBalance(handle, acctFrom, amount);
                addToBalance(handle, acctTo, amount);
            } else {
                addToBalance(handle, acctTo, amount);
                deductFromBalance(handle, acctFrom, amount);
            }
            return addTransfer(handle, acctFrom, acctTo, amount);
        });
    }

    private void deductFromBalance(Handle handle, String acct, BigDecimal amount) {
        if (!updateBalance(handle, acct, amount,
                "UPDATE Account SET Balance = Balance - :amount WHERE AccountId = :acct AND Balance > :amount"
        )) {
            if (doesAccountExist(handle, acct)) {
                throw new IllegalArgumentException(NOT_SUFFICIENT_FUNDS);
            } else {
                new IllegalArgumentException(ACCOUNT_NOT_FOUND + ": " + acct);
            }
        }
    }

    private void addToBalance(Handle handle, String acct, BigDecimal amount) {
        if (!updateBalance(handle, acct, amount,
                "UPDATE Account SET Balance = Balance + :amount WHERE AccountId = :acct"
        )) {
            new IllegalArgumentException(ACCOUNT_NOT_FOUND + ": " + acct);
        }
    }

    private boolean updateBalance(Handle handle, String acct, BigDecimal amount, String updateSql) {
        int updatedCount = handle.createUpdate(updateSql)
                .bind("acct", acct)
                .bind("amount", amount).execute();
        if (updatedCount > 1) {
            throw new IllegalStateException("Tried updating more than 2 accounts");
        }
        return updatedCount == 1;
    }
}
