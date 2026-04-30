package com.idea.service;

import com.idea.dto.AssistantChatRequest;
import com.idea.dto.AssistantTestEndRequest;
import com.idea.dto.AssistantTestStartRequest;
import com.idea.entity.AssistantTestLog;
import com.idea.entity.Result;
import com.idea.common.ErrorCode;
import com.idea.exception.BusinessException;
import com.idea.mapper.AssistantTestLogMapper;
import com.idea.vo.AssistantChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantTestLogService {

    private final AssistantTestLogMapper assistantTestLogMapper;

    public Map<String, Object> startAnonymousTest(Long userId, AssistantTestStartRequest request) {
        String sessionId = "TEST-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6);
        String participantCode = "ANON-" + sessionId.substring(sessionId.length() - 6).toUpperCase();
        String participantGroup = normalizeParticipantGroup(request == null ? null : request.getParticipantGroup());

        AssistantTestLog logEntity = new AssistantTestLog();
        logEntity.setUserId(userId);
        logEntity.setSource("usability_live");
        logEntity.setScenario("feedback_session_start");
        logEntity.setRequestText(limit("匿名反馈测试开始，角色=" + participantGroup
                + buildRoleNote(request == null ? null : request.getRoleNote()), 500));
        logEntity.setResultCode(1);
        logEntity.setBizCode(0);
        logEntity.setSuccess(1);
        logEntity.setTestSessionId(sessionId);
        logEntity.setParticipantCode(participantCode);
        logEntity.setParticipantGroup(participantGroup);
        logEntity.setTaskCode("START");
        logEntity.setTaskName("匿名反馈测试开始");
        logEntity.setCompleted(1);
        record(logEntity);

        return Map.of(
                "testSessionId", sessionId,
                "participantCode", participantCode,
                "participantGroup", participantGroup
        );
    }

    public Map<String, Object> finishAnonymousTest(Long userId, AssistantTestEndRequest request) {
        if (request == null || isBlank(request.getTestSessionId()) || isBlank(request.getParticipantCode())) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "匿名测试会话信息不完整");
        }

        AssistantTestLog logEntity = new AssistantTestLog();
        logEntity.setUserId(userId);
        logEntity.setSource("usability_live");
        logEntity.setScenario("feedback_session_end");
        logEntity.setRequestText("匿名反馈测试结束并提交评分");
        logEntity.setResultCode(1);
        logEntity.setBizCode(0);
        logEntity.setSuccess(1);
        logEntity.setTestSessionId(limit(request.getTestSessionId(), 64));
        logEntity.setParticipantCode(limit(request.getParticipantCode(), 20));
        logEntity.setParticipantGroup(normalizeParticipantGroup(request.getParticipantGroup()));
        logEntity.setTaskCode("SUMMARY");
        logEntity.setTaskName("匿名反馈测试评分");
        logEntity.setCompleted(1);
        logEntity.setUnderstandingScore(clampScore(request.getUnderstandingScore()));
        logEntity.setSatisfactionScore(clampScore(request.getSatisfactionScore()));
        logEntity.setSafetyScore(clampScore(request.getSafetyScore()));
        logEntity.setLanguageScore(clampScore(request.getLanguageScore()));
        logEntity.setUserFeedback(limit(request.getFeedback(), 500));
        record(logEntity);

        return Map.of(
                "testSessionId", logEntity.getTestSessionId(),
                "participantCode", logEntity.getParticipantCode(),
                "submitted", true
        );
    }

    public void recordRuntime(Long userId, String requestText, Result result, long durationMs, Throwable error) {
        recordRuntime(userId, null, requestText, result, durationMs, error);
    }

    public void recordRuntime(Long userId, AssistantChatRequest request, String requestText, Result result, long durationMs, Throwable error) {
        AssistantTestLog logEntity = new AssistantTestLog();
        logEntity.setUserId(userId);
        boolean inAnonymousTest = request != null && !isBlank(request.getTestSessionId());
        logEntity.setSource(inAnonymousTest ? "usability_live" : "runtime");
        logEntity.setScenario(inAnonymousTest ? "assistant_chat_test" : "assistant_chat");
        logEntity.setRequestText(limit(requestText, 500));
        logEntity.setDurationMs(Math.max(durationMs, 0));
        if (inAnonymousTest) {
            logEntity.setTestSessionId(limit(request.getTestSessionId(), 64));
            logEntity.setParticipantCode(limit(request.getParticipantCode(), 20));
            logEntity.setParticipantGroup(normalizeParticipantGroup(request.getParticipantGroup()));
            logEntity.setTaskCode(normalizeTaskCode(request.getTaskCode()));
            logEntity.setTaskName(limit(resolveTaskName(request.getTaskCode(), request.getTaskName()), 80));
            if (request.getOperationSteps() != null && request.getOperationSteps() > 0) {
                logEntity.setOperationSteps(request.getOperationSteps());
            }
            logEntity.setUnderstandingScore(clampScore(request.getUnderstandingScore()));
            logEntity.setSatisfactionScore(clampScore(request.getSatisfactionScore()));
            logEntity.setSafetyScore(clampScore(request.getSafetyScore()));
            logEntity.setLanguageScore(clampScore(request.getLanguageScore()));
            logEntity.setUserFeedback(limit(request.getUserFeedback(), 500));
        }

        if (result != null) {
            logEntity.setResultCode(result.getCode());
            logEntity.setBizCode(result.getBizCode());
            logEntity.setSuccess(result.getCode() != null && result.getCode() == 1 && error == null ? 1 : 0);
            if (inAnonymousTest) {
                logEntity.setCompleted(logEntity.getSuccess());
            }
            fillResponseFields(logEntity, result.getData());
        } else {
            logEntity.setSuccess(0);
            if (inAnonymousTest) {
                logEntity.setCompleted(0);
            }
        }

        if (error != null) {
            logEntity.setErrorMessage(limit(error.getMessage(), 500));
        }

        record(logEntity);
    }

    public void record(AssistantTestLog logEntity) {
        if (logEntity == null) {
            return;
        }
        try {
            assistantTestLogMapper.insert(logEntity);
        } catch (Exception e) {
            // 测试指标日志不能影响正常客服链路。
            log.warn("assistant test log insert failed, scenario={}, request={}",
                    logEntity.getScenario(), logEntity.getRequestText(), e);
        }
    }

    private void fillResponseFields(AssistantTestLog logEntity, Object data) {
        if (data == null) {
            logEntity.setResponseChars(0);
            return;
        }
        if (data instanceof AssistantChatResponse response) {
            String reply = response.getReply() == null ? "" : response.getReply();
            logEntity.setResponseChars(reply.length());
            if (response.getTrace() != null) {
                logEntity.setIntent(response.getTrace().getIntent());
                logEntity.setTraceTool(response.getTrace().getTool());
                logEntity.setTraceStatus(response.getTrace().getStatus());
            }
            return;
        }
        logEntity.setResponseChars(String.valueOf(data).length());
    }

    private String limit(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String normalizeParticipantGroup(String participantGroup) {
        if (isBlank(participantGroup)) {
            return "未说明角色";
        }
        return limit(participantGroup.trim(), 50);
    }

    private String buildRoleNote(String roleNote) {
        if (isBlank(roleNote)) {
            return "";
        }
        return "，说明=" + roleNote.trim();
    }

    private Integer clampScore(Integer score) {
        if (score == null) {
            return null;
        }
        return Math.max(1, Math.min(5, score));
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String normalizeTaskCode(String taskCode) {
        if (isBlank(taskCode)) {
            return "UNSELECTED";
        }
        String code = taskCode.trim().toUpperCase();
        if (code.matches("U[1-8]")) {
            return code;
        }
        return "UNSELECTED";
    }

    private String resolveTaskName(String taskCode, String taskName) {
        if (!isBlank(taskName)) {
            return taskName.trim();
        }
        return switch (normalizeTaskCode(taskCode)) {
            case "U1" -> "查询账户余额";
            case "U2" -> "查询最近5条支出流水";
            case "U3" -> "银行卡安全FAQ";
            case "U4" -> "转账到账FAQ";
            case "U5" -> "语音查询余额";
            case "U6" -> "支出统计图表";
            case "U7" -> "转账后取消";
            case "U8" -> "投诉情绪安抚";
            default -> "未选择测试任务";
        };
    }
}
