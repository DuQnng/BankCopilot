package com.idea.dto;

import lombok.Data;

@Data
public class AssistantTestEndRequest {
    private String testSessionId;
    private String participantCode;
    private String participantGroup;
    private Integer understandingScore;
    private Integer satisfactionScore;
    private Integer safetyScore;
    private Integer languageScore;
    private String feedback;
}
