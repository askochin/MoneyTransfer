package com.dev.moneytransfer.dao;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.*;

class DeadlockSafeTransferDaoTest extends AbstractTransferDaoTest {

    protected AbstractTransferDao createDao(Jdbi jdbi) {
        return new DeadlockSafeTransferDao(jdbi);
    }

    @Test
    public void shouldAvoidDeadlockWithOppositeTransfers() throws InterruptedException, ExecutionException, TimeoutException {
        final int transfersCount = 100;
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(
                () -> { for (int i = 0; i < transfersCount; i++) dao.transfer("a1", "a2", new BigDecimal("0.01")); }
        );
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(
                () -> { for (int i = 0; i < transfersCount; i++) dao.transfer("a2", "a1", new BigDecimal("0.01")); }
        );
        f1.get(1, TimeUnit.MINUTES);
        f2.get(1, TimeUnit.MINUTES);
        assertEquals(transfersCount * 2, actualTransfers().size());
    }

    @Test
    public void shouldThrowExceptionIfFirstAccountNotFound() {
        assertEquals(1, dao.transfer("ax", "a2", new BigDecimal("10")));
    }
}