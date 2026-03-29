package com.idea.dto;

import lombok.Data;

@Data
public class StatisticsQueryDTO {
    private Long accountId;
    private String txnType;           // 收入 / 支出
    private String metric;            // sum / count / max / trend
    private String groupBy;           // none / day / month
    private String startTime;
    private String endTime;
    private Integer rangeValue;       // 最近N天 / 最近N月等
    private Boolean comparePrevious;  // 是否比较上一个周期
}
