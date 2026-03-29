package com.idea.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsPointVO {
    private String label;        // 例如 2026-01 / 2026-03-22
    private BigDecimal value;    // 该时间点统计值
}
