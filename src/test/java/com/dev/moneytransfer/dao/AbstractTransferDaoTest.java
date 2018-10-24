package com.dev.moneytransfer.dao;

import com.dev.moneytransfer.dao.helper.Account;
import com.dev.moneytransfer.dao.helper.Transfer;
import com.dev.moneytransfer.dao.helper.TransferDaoTestHelper;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

abstract class AbstractTransferDaoTest {

    AbstractTransferDao dao;
    TransferDaoTestHelper helper;

    private final Account[] initAcounts = {acct("a1", "100.00"), acct("a2", "200.00"), acct("a3", "300.00")};

    abstract AbstractTransferDao createDao(Jdbi jdbi);

    @BeforeEach
    public void prepareTest() throws IOException {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:accts;DB_CLOSE_DELAY=-1;MULTI_THREADED=1");
        dao = createDao(jdbi);
        helper = new TransferDaoTestHelper(jdbi);
        helper.initDb("schema.sql");
        helper.addAccounts(initAcounts);
    }

    @AfterEach
    public void clearDb() {
        helper.clearDb();
    }

    @Test
    public void shouldTransferMoney() {
        long transferId = dao.transfer("a1", "a2", new BigDecimal("99.9"));
        assertEquals(1, transferId);
        assertEquals(
            expectedAccounts(acct("a1", "0.10"), acct("a2", "299.90"), acct("a3", "300.00")),
            actualAccounts());
        assertEquals(
            expectedTransfers(trsfr(1, "a1", "a2", "99.90")),
            actualTransfers());
    }

    @Test
    public void shouldIncrementTransferId() {
        long transferId1 = dao.transfer("a1", "a2", new BigDecimal("10"));
        long transferId2 = dao.transfer("a2", "a3", new BigDecimal("20"));
        long transferId3 = dao.transfer("a3", "a1", new BigDecimal("50"));
        assertTrue(transferId1 < transferId2);
        assertTrue(transferId2 < transferId3);
    }

    @Test
    public void shouldThrowExceptionIfFirstAccountNotFound() {
        assertThrows(
            IllegalArgumentException.class,
            () -> dao.transfer("ax", "a2", new BigDecimal("10")),
            "Account not found: ax"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    @Test
    public void shouldThrowExceptionIfSecondAccountNotFound() {
        assertThrows(
            IllegalArgumentException.class,
            () -> dao.transfer("a1", "ax", new BigDecimal("10")),
            "Account not found: ax"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    @Test
    public void shouldThrowExceptionIfNotSufficientFunds() {
        assertThrows(
            IllegalArgumentException.class,
            () -> dao.transfer("a1", "a2", new BigDecimal("101")),
            "Not sufficient funds"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    //==================== Instrumental methods ========================//

    Account acct(String id, String balance) {
        return new Account(id, new BigDecimal(balance));
    }

    Transfer trsfr(long id, String acctFrom, String acctTo, String amount) {
        return new Transfer(id, acctFrom, acctTo, new BigDecimal(amount));
    }

    Set<Account> actualAccounts() {
        return helper.getAllAccounts();
    }

    Set<Account> expectedAccounts(Account ... accounts) {
        return new HashSet<>(Arrays.asList(accounts));
    }

    Set<Transfer> actualTransfers() {
        return helper.getAllTransfers();
    }

    Set<Transfer> expectedTransfers(Transfer... transfers) {
        return new HashSet<>(Arrays.asList(transfers));
    }
}