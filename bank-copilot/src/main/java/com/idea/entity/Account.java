package com.idea.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("account")
public class Account {

    private Long id;
    private Long userId;
    private String accountName;
    private String accountNo;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}