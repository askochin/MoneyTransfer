package com.dev.moneytransfer;

import org.jdbi.v3.core.Jdbi;

public class DbUtils {

    public static void initDbStructure(Jdbi jdbi) {
        jdbi.useHandle(handle -> {
            handle.execute("CREATE TABLE Account (AccountId VARCHAR(64) PRIMARY KEY, Balance DECIMAL(20,2)");
        });
    }
}
