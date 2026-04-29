package com.idea.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FaqKnowledge {
    private Long id;
    private String category;
    private String standardQuestion;
    private String aliases;
    private String keywords;
    private String answer;
    private Integer status;
    private Integer priority;
    private String source;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
