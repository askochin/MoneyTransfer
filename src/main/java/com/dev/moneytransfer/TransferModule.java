package com.dev.moneytransfer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;


public class TransferModule extends AbstractModule {

    private final String jdbcUrl;
    private final int maxConnectionPoolSize;
    private final String dbInitScript;

    TransferModule(String jdbcUrl, int maxConnectionPoolSize, String dbInitScript) {
        this.jdbcUrl = jdbcUrl;
        this.maxConnectionPoolSize = maxConnectionPoolSize;
        this.dbInitScript = dbInitScript;
    }

    @Override
    protected void configure() {
        bind(Application.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    private Jdbi provideObjectMapper() throws IOException {
        HikariConfig jdbcConfig = new HikariConfig();
        jdbcConfig.setMaximumPoolSize(maxConnectionPoolSize);
        jdbcConfig.setJdbcUrl(jdbcUrl);
        Jdbi jdbi = Jdbi.create(new HikariDataSource(jdbcConfig));
        if (dbInitScript != null) {
            DbUtil.initDb(jdbi, dbInitScript);
        }
        return jdbi;
    }
}
