package com.idea.dto;

import lombok.Data;

@Data
public class PayeeQueryDTO {
    private Integer page;
    private Integer size;
    private String payeeName;
    private String accountNo;
    private String alias;
}

