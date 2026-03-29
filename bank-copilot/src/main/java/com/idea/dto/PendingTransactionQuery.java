package com.idea.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingTransactionQuery {
    private String type;              // 收入 / 支出
    private Integer day;              // 例如 17
    private Integer month;            // 后续补充
    private Integer year;             // 可默认当前年
    private Integer limit;            // 最近几条
    private String rawTimeText;       // 原始时间表达，便于调试
    private LocalDateTime createdAt;
}
