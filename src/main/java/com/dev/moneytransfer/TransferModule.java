package com.dev.moneytransfer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

public class TransferModule extends AbstractModule {

    private static final String JDBC_URL_IN_MEMORY_H2 = "jdbc:h2:mem:accts";

    private final String jdbcUrl;

    TransferModule(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl == null ? JDBC_URL_IN_MEMORY_H2 : jdbcUrl;
    }

    @Override
    protected void configure() {
        bind(Application.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    private Jdbi provideObjectMapper() {
        Jdbi jdbi = Jdbi.create(jdbcUrl);
        if (JDBC_URL_IN_MEMORY_H2.equals(jdbcUrl)) {
            DbUtils.initDbStructure(jdbi);
        }
        return jdbi;
    }
}
