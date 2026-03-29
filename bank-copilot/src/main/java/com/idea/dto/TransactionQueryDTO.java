package com.idea.dto;

import lombok.Data;

@Data
public class TransactionQueryDTO {
    private Long accountId;
    private Integer page;
    private Integer size;

    private String type;       // 收入 / 支出
    private String startTime;  // 你现在是 String，就先保持 String
    private String endTime;
}
