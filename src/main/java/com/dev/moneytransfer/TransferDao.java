package com.dev.moneytransfer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

import java.math.BigDecimal;
import java.util.List;

@Singleton
public class TransferDao {

    private final Jdbi jdbi;

    @Inject
    TransferDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public boolean transfer(String acctFrom, String acctTo, BigDecimal sum) {
        return jdbi.inTransaction(handle -> {

            int updatedCount = handle
                    .createUpdate(
                            "UPDATE Account SET " +
                            "Balance = CASE WHEN AccountId = :acctFrom THEN Balance - :sum ELSE Balance + :sum END " +
                            "WHERE AccountId = :acctFrom AND Balance > :sum OR AccountId = :acctTo")
                    .bind("acctFrom", acctFrom)
                    .bind("acctTo", acctTo)
                    .bind("sum", sum).execute();

            if (updatedCount > 2) {
                throw new IllegalStateException("Tried updating more than 2 accounts");
            }
            if (updatedCount == 2) {
                handle.commit();
                return true;
            } else {
                handle.rollback();
                return false;
            }
        });
    }

    public List<String> getExistingAccountsOf(String acct1, String acct2) {
        return jdbi.withHandle(handle ->
                handle.createQuery(
                        "SELECT AccountId FROM Account WHERE AccountId = :acct1 OR AccountId = :acct2")
                .bind("acct1", acct1)
                .bind("acct2", acct2)
                .mapTo(String.class).list()
        );
    }
}
