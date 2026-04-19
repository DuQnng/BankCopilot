package com.idea.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssistantChatResponse {
    private String reply;

    // AI返回的高级图表或下载功能
    private String chartType; // "line", "bar", "pie", "number"
    private Object chartData; // List<StatisticsPointVO> 或具体数值
    private String exportUrl; // 原始数据下载链接
    private TraceInfo trace;  // AI过程元信息
    private Map<String, Object> explain; // 统计口径/解释字段

    public AssistantChatResponse(String reply) {
        this.reply = reply;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TraceInfo {
        private String intent;
        private String tool;
        private String status;
        private String confidence;
        private String timeRange;
        private List<String> steps;
    }
}
