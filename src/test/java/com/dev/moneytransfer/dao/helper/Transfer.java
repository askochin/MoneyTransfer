package com.dev.moneytransfer.dao.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    private long transferId;
    private String sourceAccountId;
    private String destAccountId;
    private BigDecimal amount;
}
