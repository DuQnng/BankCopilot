package com.idea.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionVO {
    private String tradeTime;
    private String accountNo;
    private String counterpartyAccountNo;
    private String type;
    private BigDecimal amount;
    private String description;
}
