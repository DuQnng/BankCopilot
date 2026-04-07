package com.idea.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssistantChatResponse {
    private String reply;

    // AI返回的高级图表或下载功能
    private String chartType; // "line", "bar", "pie", "number"
    private Object chartData; // List<StatisticsPointVO> 或具体数值
    private String exportUrl; // 原始数据下载链接

    public AssistantChatResponse(String reply) {
        this.reply = reply;
    }
}
