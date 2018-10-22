package com.dev.moneytransfer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;

public class TransferModule extends AbstractModule {

    private static final String JDBC_URL_IN_MEMORY_H2 =
            "jdbc:h2:mem:accts;DB_CLOSE_DELAY=-1";

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
    private Jdbi provideObjectMapper() throws IOException {
        Jdbi jdbi = Jdbi.create(JDBC_URL_IN_MEMORY_H2);
        jdbi.useHandle(handle -> handle.createScript(
                Resources.toString(Resources.getResource("src/test/resources/init.sql"), Charsets.UTF_8)
                ).execute()
        );

        //DbUtils.initDbStructure(jdbi);
        return jdbi;
    }
}
