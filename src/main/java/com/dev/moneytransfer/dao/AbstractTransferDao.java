package com.dev.moneytransfer.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.math.BigDecimal;


@Singleton
abstract public class AbstractTransferDao {

    static final String ACCOUNT_NOT_FOUND = "Account not found";
    static final String NOT_SUFFICIENT_FUNDS = "Not sufficient funds";

    private final Jdbi jdbi;

    @Inject
    AbstractTransferDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * Transfer amount of money from acctFrom to acctTo.
     * @return The ID of transfer
     * @throws IllegalArgumentException if any account is not found or there's not sufficient funds at acctFrom
     */
    public abstract long transfer(String acctFrom, String acctTo, BigDecimal amount);


    protected Jdbi jdbi() {
        return jdbi;
    }

    protected long addTransfer(Handle handle, String acctFrom, String acctTo, BigDecimal amount) {
        return handle.createUpdate(
                "INSERT INTO Transfer(SourceAccountId, DestAccountId, Amount) " +
                "VALUES(:acctFrom, :acctTo, :amount)")
            .bind("acctFrom", acctFrom)
            .bind("acctTo", acctTo)
            .bind("amount", amount)
            .executeAndReturnGeneratedKeys("TransferId")
            .mapTo(Long.class).findOnly();
    }

    protected boolean doesAccountExist(Handle handle, String acct) {
        return !handle.createQuery("SELECT AccountId FROM Account WHERE AccountId = :acct")
            .bind("acct", acct)
            .mapTo(String.class).list().isEmpty();
    }
}
