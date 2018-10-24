package com.dev.moneytransfer.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.math.BigDecimal;

import static org.jdbi.v3.core.transaction.TransactionIsolationLevel.READ_COMMITTED;

@Singleton
public class SimpleTransferDao extends AbstractTransferDao {

    @Inject
    SimpleTransferDao(Jdbi jdbi) {
        super(jdbi);
    }

    /**
     * Transfer amount of money from acctFrom to acctTo.
     * The order of account record update is not guaranteed.
     * @return The ID of transfer
     * @throws IllegalArgumentException if any account is not found or there's not sufficient funds at acctFrom
     */
    public long transfer(String acctFrom, String acctTo, BigDecimal amount) {

        return jdbi().inTransaction(READ_COMMITTED, handle -> {

            updateBalance(handle, acctFrom, acctTo, amount);
            return addTransfer(handle, acctFrom, acctTo, amount);
        });
    }

    private void updateBalance(Handle handle, String acctFrom, String acctTo, BigDecimal amount) {
        int updatedCount = handle.createUpdate(
                "UPDATE Account SET " +
                        "Balance = CASE WHEN AccountId = :acctFrom THEN Balance - :amount ELSE Balance + :amount END " +
                        "WHERE AccountId = :acctFrom AND Balance > :amount OR AccountId = :acctTo")
                .bind("acctFrom", acctFrom)
                .bind("acctTo", acctTo)
                .bind("amount", amount).execute();
        if (updatedCount > 2) {
            throw new IllegalStateException("Tried updating more than 2 accounts");
        }
        if (updatedCount < 2) {
            if (!doesAccountExist(handle, acctTo)) {
                throw new IllegalArgumentException(ACCOUNT_NOT_FOUND + ": " + acctTo);
            }
            if (!doesAccountExist(handle, acctFrom)) {
                throw new IllegalArgumentException(ACCOUNT_NOT_FOUND + ": " + acctFrom);
            }
            throw new IllegalArgumentException(NOT_SUFFICIENT_FUNDS);
        }
    }
}
