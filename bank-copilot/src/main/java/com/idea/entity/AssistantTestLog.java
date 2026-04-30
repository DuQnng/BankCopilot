package com.idea.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssistantTestLog {
    private Long id;
    private Long userId;
    private String source;
    private String scenario;
    private String requestText;
    private String intent;
    private String traceTool;
    private String traceStatus;
    private Integer resultCode;
    private Integer bizCode;
    private Integer success;
    private Long durationMs;
    private Integer responseChars;
    private String errorMessage;
    private String testSessionId;
    private String participantCode;
    private String participantGroup;
    private String taskCode;
    private String taskName;
    private Integer completed;
    private Integer operationSteps;
    private Integer understandingScore;
    private Integer satisfactionScore;
    private Integer safetyScore;
    private Integer languageScore;
    private String userFeedback;
    private LocalDateTime createTime;
}
