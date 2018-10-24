package com.dev.moneytransfer;

import com.dev.moneytransfer.dao.Account;
import com.dev.moneytransfer.dao.Transfer;
import com.dev.moneytransfer.dao.TransferDaoTestHelper;
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

public class TransferDaoTest {

    private TransferDao transferDao;
    private TransferDaoTestHelper helper;

    private final Account[] initAcounts = {acct("a1", "100.00"), acct("a2", "200.00"), acct("a3", "300.00")};

    @BeforeEach
    public void prepareTest() throws IOException {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:accts;DB_CLOSE_DELAY=-1;MULTI_THREADED=TRUE");
        transferDao = new TransferDao(jdbi);
        helper = new TransferDaoTestHelper(jdbi);
        helper.initSchema();
        helper.addAccounts(initAcounts);
    }

    @AfterEach
    public void clearDb() {
        helper.clearDb();
    }

    @Test
    public void shouldTransferMoney() {
        long transferId = transferDao.transfer("a1", "a2", new BigDecimal("99.9"));
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
        long transferId1 = transferDao.transfer("a1", "a2", new BigDecimal("10"));
        long transferId2 = transferDao.transfer("a2", "a3", new BigDecimal("20"));
        long transferId3 = transferDao.transfer("a3", "a1", new BigDecimal("50"));
        assertTrue(transferId1 < transferId2);
        assertTrue(transferId2 < transferId3);
    }

    @Test
    public void shouldThrowExceptionIfFirstAccountNotFound() {
        assertThrows(
            IllegalArgumentException.class,
            () -> transferDao.transfer("ax", "a2", new BigDecimal("10")),
            "Account not found: ax"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    @Test
    public void shouldThrowExceptionIfSecondAccountNotFound() {
        assertThrows(
            IllegalArgumentException.class,
            () -> transferDao.transfer("a1", "ax", new BigDecimal("10")),
            "Account not found: ax"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    @Test
    public void shouldThrowExceptionIfBothAccountsNotFound() {
        assertThrows(
            IllegalArgumentException.class,
            () -> transferDao.transfer("ax", "ay", new BigDecimal("10")),
            "Account not found: ax, ay"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    @Test
    public void shouldThrowExceptionIfNotSufficientFunds() {
        assertThrows(
            IllegalArgumentException.class,
            () -> transferDao.transfer("a1", "a2", new BigDecimal("101")),
            "Not sufficient funds"
        );
        assertEquals(expectedAccounts(initAcounts), actualAccounts());
        assertEquals(emptySet(), actualTransfers());
    }

    //==================== Instrumental methods ========================//

    private Account acct(String id, String balance) {
        return new Account(id, new BigDecimal(balance));
    }

    private Transfer trsfr(long id, String acctFrom, String acctTo, String amount) {
        return new Transfer(id, acctFrom, acctTo, new BigDecimal(amount));
    }

    private Set<Account> actualAccounts() {
        return helper.getAllAccounts();
    }

    private Set<Account> expectedAccounts(Account ... accounts) {
        return new HashSet<>(Arrays.asList(accounts));
    }

    private Set<Transfer> actualTransfers() {
        return helper.getAllTransfers();
    }

    private Set<Transfer> expectedTransfers(Transfer... transfers) {
        return new HashSet<>(Arrays.asList(transfers));
    }
}