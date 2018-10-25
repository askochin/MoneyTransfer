package com.dev.moneytransfer;

import com.dev.moneytransfer.dao.JdbcTransferDao;
import com.dev.moneytransfer.dao.TransferDao;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;


public class TransferModule extends AbstractModule {

    private final String jdbcUrl;
    private final String dbInitScript;

    TransferModule(String jdbcUrl, String dbInitScript) {
        this.jdbcUrl = jdbcUrl;
        this.dbInitScript = dbInitScript;
    }

    @Override
    protected void configure() {
        bind(Application.class).in(Singleton.class);
        bind(TransferDao.class).to(JdbcTransferDao.class);
    }

    @Provides
    @Singleton
    private Jdbi provideObjectMapper() throws IOException {
        Jdbi jdbi = Jdbi.create(jdbcUrl);
        if (dbInitScript != null) {
            DbUtil.initDb(jdbi, dbInitScript);
        }
        return jdbi;
    }
}
