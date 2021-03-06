package com.dev.moneytransfer.dao;

import com.google.common.io.Resources;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;

public class TransferDaoTestHelper {

    private final Jdbi jdbi;

    public TransferDaoTestHelper(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void initSchema() throws IOException {
        String scriptContent = Resources.toString(getResource("schema.sql"), UTF_8);
        jdbi.useHandle(handle -> handle.createScript(scriptContent).execute());
    }

    public void clearDb() {
        jdbi.useHandle(handle -> handle.createUpdate("drop all objects").execute());
    }

    public void addAccounts(Account ... accounts) {
        for (Account account : accounts) {
            addAccount(account);
        }
    }

    public void addAccount(Account account) {
        jdbi.useHandle(handle ->
                handle.createUpdate("insert into Account values(:accountId, :balance)").bindBean(account).execute()
        );
    }

    public Set<Account> getAllAccounts() {
        return new HashSet<>(
                jdbi.withHandle(handle -> handle.createQuery("select * from Account").mapToBean(Account.class).list())
        );
    }

    public Set<Transfer> getAllTransfers() {
        return new HashSet<>(
                jdbi.withHandle(handle -> handle.createQuery("select * from Transfer").mapToBean(Transfer.class).list())
        );
    }
}
