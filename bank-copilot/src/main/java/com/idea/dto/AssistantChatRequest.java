package com.idea.dto;

import lombok.Data;

@Data
public class AssistantChatRequest {
    private String message;
    private String testSessionId;
    private String participantCode;
    private String participantGroup;
    private String taskCode;
    private String taskName;
    private Integer operationSteps;
    private Integer understandingScore;
    private Integer satisfactionScore;
    private Integer safetyScore;
    private Integer languageScore;
    private String userFeedback;
}
