package com.idea.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferValidateVO {
    private String fromAccountNoMasked; // 付款账号脱敏
    private String toAccountNoMasked;   // 收款账号脱敏
    private BigDecimal amount;          // 转账金额
    private BigDecimal balance;         // 当前余额（付款账户）
    private String description;         // 备注（可选）
}
