package com.dev.moneytransfer.dao;

import org.jdbi.v3.core.Jdbi;


class SimpleTransferDaoTest extends AbstractTransferDaoTest {
    SimpleTransferDao createDao(Jdbi jdbi) {
        return new SimpleTransferDao(jdbi);
    }
}