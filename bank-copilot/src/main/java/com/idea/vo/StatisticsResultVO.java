package com.idea.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsResultVO {
    private String title;
    private String metric;
    private String txnType;
    private BigDecimal value;              // sum / max 时可用
    private Long count;                    // count 时可用
    private List<StatisticsPointVO> trendList; // trend 时可用
    private String summary;                // 给前端/聊天用的总结
    
    // 扩展多图表总览功能所需字段
    private List<StatisticsPointVO> incomeExpenseBar; 
    private List<StatisticsPointVO> incomePie;        
    private List<StatisticsPointVO> expensePie;       
}
