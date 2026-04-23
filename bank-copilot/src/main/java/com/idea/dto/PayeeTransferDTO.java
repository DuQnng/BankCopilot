package com.idea.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayeeTransferDTO {
    private BigDecimal amount;
    private String description;
}

