package com.idea.assistant;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PendingTransfer {
    private Long payeeId;
    private String payeeAlias;
    private String toAccountNo;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
}
