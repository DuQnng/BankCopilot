package com.idea.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {

    private String toAccountNo; // 收款账号
    private BigDecimal amount;  // 转账金额
    private String description; // 备注
}
