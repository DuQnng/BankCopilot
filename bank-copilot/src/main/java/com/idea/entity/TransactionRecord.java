package com.idea.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction_record")
public class TransactionRecord {

    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime tradeTime;
}
