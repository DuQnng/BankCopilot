package com.idea.controller;

import com.idea.common.ErrorCode;
import com.idea.dto.AssistantChatRequest;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.service.AssistantService;
import com.idea.utils.CurrentHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public Result chat(@RequestBody AssistantChatRequest req) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return assistantService.chat(userId.longValue(), req);
    }
}
