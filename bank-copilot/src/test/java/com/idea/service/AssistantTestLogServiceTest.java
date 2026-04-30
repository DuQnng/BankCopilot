package com.idea.service;

import com.idea.dto.AssistantChatRequest;
import com.idea.entity.AssistantTestLog;
import com.idea.entity.Result;
import com.idea.mapper.AssistantTestLogMapper;
import com.idea.vo.AssistantChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssistantTestLogServiceTest {

    @Mock
    private AssistantTestLogMapper assistantTestLogMapper;

    @InjectMocks
    private AssistantTestLogService assistantTestLogService;

    @Test
    void recordRuntimeShouldExtractTraceAndResponseMetrics() {
        AssistantChatResponse response = new AssistantChatResponse("为你查询到账户信息如下");
        response.setTrace(new AssistantChatResponse.TraceInfo(
                "balance",
                "account_info",
                "completed",
                "high",
                "",
                List.of("识别为余额/账户查询意图", "查询账户信息")
        ));
        Result result = Result.success(response);

        assistantTestLogService.recordRuntime(1L, "查一下我的余额", result, 23L, null);

        ArgumentCaptor<AssistantTestLog> captor = ArgumentCaptor.forClass(AssistantTestLog.class);
        verify(assistantTestLogMapper).insert(captor.capture());
        AssistantTestLog log = captor.getValue();
        assertEquals(1L, log.getUserId());
        assertEquals("runtime", log.getSource());
        assertEquals("assistant_chat", log.getScenario());
        assertEquals("balance", log.getIntent());
        assertEquals("account_info", log.getTraceTool());
        assertEquals("completed", log.getTraceStatus());
        assertEquals(1, log.getSuccess());
        assertEquals(23L, log.getDurationMs());
        assertEquals(response.getReply().length(), log.getResponseChars());
    }

    @Test
    void recordRuntimeShouldStoreAnonymousTaskScoresAndFeedback() {
        AssistantChatRequest request = new AssistantChatRequest();
        request.setTestSessionId("TEST-1");
        request.setParticipantCode("ANON-1");
        request.setParticipantGroup("计算机专业学生");
        request.setTaskCode("U1");
        request.setTaskName("查询账户余额");
        request.setOperationSteps(2);
        request.setUnderstandingScore(5);
        request.setSatisfactionScore(4);
        request.setSafetyScore(5);
        request.setLanguageScore(4);
        request.setUserFeedback("余额展示清楚。");

        assistantTestLogService.recordRuntime(
                1L,
                request,
                "查一下我的余额",
                Result.success(new AssistantChatResponse("ok")),
                20L,
                null
        );

        ArgumentCaptor<AssistantTestLog> captor = ArgumentCaptor.forClass(AssistantTestLog.class);
        verify(assistantTestLogMapper).insert(captor.capture());
        AssistantTestLog log = captor.getValue();
        assertEquals("usability_live", log.getSource());
        assertEquals("assistant_chat_test", log.getScenario());
        assertEquals("TEST-1", log.getTestSessionId());
        assertEquals("U1", log.getTaskCode());
        assertEquals(2, log.getOperationSteps());
        assertEquals(5, log.getUnderstandingScore());
        assertEquals(4, log.getSatisfactionScore());
        assertEquals(5, log.getSafetyScore());
        assertEquals(4, log.getLanguageScore());
        assertEquals("余额展示清楚。", log.getUserFeedback());
    }

    @Test
    void recordShouldNotThrowWhenMetricTableIsUnavailable() {
        doThrow(new RuntimeException("table not found")).when(assistantTestLogMapper).insert(any(AssistantTestLog.class));

        assertDoesNotThrow(() -> assistantTestLogService.recordRuntime(
                1L,
                "查一下我的余额",
                Result.success(new AssistantChatResponse("ok")),
                10L,
                null
        ));
    }
}
