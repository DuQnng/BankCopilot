package com.idea.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayeeContact {
    private Long id;
    private Long userId;
    private String payeeName;
    private String accountNo;
    private String alias;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

