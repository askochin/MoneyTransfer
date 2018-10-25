package com.dev.moneytransfer;

import com.dev.moneytransfer.dao.JdbcTransferDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private JdbcTransferDao dao;

    private TransferService service;

    private static final long NEW_TRANSFER_ID = 1;

    @BeforeEach
    void initService() {
        service = new TransferService(dao);
    }

    @Test
    void shouldTransferMoney() {
        when(dao.transfer("a1", "a2", new BigDecimal("10.00"))).thenReturn(NEW_TRANSFER_ID);
        long result = service.transfer("a1", "a2", new BigDecimal("10"));
        assertEquals(NEW_TRANSFER_ID, result);
        verify(dao, times(1)).transfer(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    void shouldRoundAmountUp() {
        when(dao.transfer("a1", "a2", new BigDecimal("10.56"))).thenReturn(NEW_TRANSFER_ID);
        long result = service.transfer("a1", "a2", new BigDecimal("10.555"));
        assertEquals(NEW_TRANSFER_ID, result);
    }

    @Test
    void shouldRoundAmountDown() {
        when(dao.transfer("a1", "a2", new BigDecimal("10.55"))).thenReturn(NEW_TRANSFER_ID);
        long result = service.transfer("a1", "a2", new BigDecimal("10.5549"));
        assertEquals(NEW_TRANSFER_ID, result);
    }

    @Test
    public void shouldNotTransferNegativeAmount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.transfer("a1", "a2", new BigDecimal("-10")),
            "Negative or zero amount"
        );
        verifyZeroInteractions(dao);
    }

    @Test
    public void shouldNotTransferAmountRoundedToZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer("a1", "a2", new BigDecimal("0.004")),
                "Negative or zero amount"
        );
        verifyZeroInteractions(dao);
    }

    @Test
    public void shouldNotTransferToTheSameAccount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer("a1", "a1", new BigDecimal("10")),
                "Equal accounts"
        );
        verifyZeroInteractions(dao);
    }

    @Test
    void shouldAvoidDeadlockWithOppositeAccountTransfers() throws InterruptedException, ExecutionException, TimeoutException {
        final int transfersCount = 1000;
        runAsync(() -> {
            for (int i = 0; i < transfersCount; i++)
                service.transfer("a1", "a2", new BigDecimal("10"));
        }).get(10, SECONDS);
        runAsync(() -> {
            for (int i = 0; i < transfersCount; i++)
                service.transfer("a2", "a1", new BigDecimal("10"));
        }).get(10, SECONDS);
        verify(dao, times(transfersCount * 2)).transfer(anyString(), anyString(), any(BigDecimal.class));
    }
}