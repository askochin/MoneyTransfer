package com.dev.moneytransfer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.jdbi.v3.core.transaction.TransactionIsolationLevel.READ_COMMITTED;

@Singleton
public class TransferDao {

    private final Jdbi jdbi;

    @Inject
    TransferDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public long transfer(String acctFrom, String acctTo, BigDecimal amount) {

        return jdbi.inTransaction(READ_COMMITTED, handle -> {

            int updatedCount = updateBalance(handle, acctFrom, acctTo, amount);

            if (updatedCount > 2) {
                throw new IllegalStateException("Tried updating more than 2 accounts");
            }
            if (updatedCount < 2) {
                List<String> existingAccts = getExistingAccountsOf(handle, acctFrom, acctTo);
                List<String> notFoundAccts = of(acctFrom, acctTo).filter(acct -> !existingAccts.contains(acct)).collect(toList());
                if (!notFoundAccts.isEmpty()) {
                    throw new IllegalArgumentException("Account not found: " + String.join(", ", notFoundAccts));
                }
                throw new IllegalArgumentException("Not sufficient funds");
            }

            return addTransfer(handle, acctFrom, acctTo, amount);
        });
    }

    private int updateBalance(Handle handle, String acctFrom, String acctTo, BigDecimal amount) {
        return handle.createUpdate(
                "UPDATE Account SET " +
                "Balance = CASE WHEN AccountId = :acctFrom THEN Balance - :amount ELSE Balance + :amount END " +
                "WHERE AccountId = :acctFrom AND Balance > :amount OR AccountId = :acctTo")
            .bind("acctFrom", acctFrom)
            .bind("acctTo", acctTo)
            .bind("amount", amount).execute();
    }

    private long addTransfer(Handle handle, String acctFrom, String acctTo, BigDecimal amount) {
        return handle.createUpdate(
                "INSERT INTO Transfer(SourceAccountId, DestAccountId, Amount) " +
                "VALUES(:acctFrom, :acctTo, :amount)")
            .bind("acctFrom", acctFrom)
            .bind("acctTo", acctTo)
            .bind("amount", amount)
            .executeAndReturnGeneratedKeys("TransferId")
            .mapTo(Long.class).findOnly();
    }

    private List<String> getExistingAccountsOf(Handle handle, String acct1, String acct2) {
        return handle.createQuery(
                "SELECT AccountId FROM Account WHERE AccountId = :acct1 OR AccountId = :acct2")
            .bind("acct1", acct1)
            .bind("acct2", acct2)
            .mapTo(String.class).list();
    }
}
