package com.dev.moneytransfer.dao;

import java.math.BigDecimal;

public interface TransferDao {

    /**
     * Transfer amount of money from acctFrom to acctTo.
     * @return The ID of transfer
     * @throws IllegalArgumentException if any of accounts is not found or there's not sufficient funds on acctFrom
     */
    long transfer(String acctFrom, String acctTo, BigDecimal amount);
}
