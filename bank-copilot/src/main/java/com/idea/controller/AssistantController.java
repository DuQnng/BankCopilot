package com.idea.controller;

import com.idea.common.ErrorCode;
import com.idea.dto.AssistantChatRequest;
import com.idea.dto.AssistantTestEndRequest;
import com.idea.dto.AssistantTestStartRequest;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.service.AssistantService;
import com.idea.service.AssistantTestLogService;
import com.idea.utils.CurrentHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;
    private final AssistantTestLogService assistantTestLogService;

    @PostMapping("/chat")
    public Result chat(@RequestBody AssistantChatRequest req) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return assistantService.chat(userId.longValue(), req);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AssistantChatRequest req) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return assistantService.chatStream(userId.longValue(), req);
    }

    @GetMapping("/brief")
    public Result brief(@RequestParam(defaultValue = "week") String period) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return assistantService.getBrief(userId.longValue(), period);
    }

    @PostMapping("/test/start")
    public Result startAnonymousTest(@RequestBody(required = false) AssistantTestStartRequest req) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return Result.success(assistantTestLogService.startAnonymousTest(userId.longValue(), req));
    }

    @PostMapping("/test/end")
    public Result finishAnonymousTest(@RequestBody AssistantTestEndRequest req) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return Result.success(assistantTestLogService.finishAnonymousTest(userId.longValue(), req));
    }
}
