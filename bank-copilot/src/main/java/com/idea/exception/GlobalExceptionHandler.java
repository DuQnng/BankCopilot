package com.idea.exception;

import com.idea.common.ErrorCode;
import com.idea.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 业务异常：可控、可解释（给客服/前端判断）
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        log.warn("业务异常 bizCode={}, msg={}", e.getBizCode(), e.getMessage());
        return Result.error(e.getBizCode(), e.getMessage());
    }

    // 2) 唯一键冲突：注册重复、卡号重复等
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一键冲突", e);

        // 给用户的提示，别把数据库细节直接透出
        String userMsg = "数据已存在，请勿重复提交";

        // 你想保留原本“xxx已存在”解析也可以，但要防止 substring 崩
        String message = e.getMessage();
        if (message != null) {
            int i = message.indexOf("Duplicate entry");
            if (i >= 0) {
                String errMsg = message.substring(i);
                String[] arr = errMsg.split(" ");
                if (arr.length >= 3) {
                    userMsg = arr[2] + " 已存在";
                }
            }
        }

        // 这里我先用 PARAM_INVALID，你也可以后面加一个 DUPLICATE_KEY(1004,...)
        return Result.error(ErrorCode.PARAM_INVALID.code(), userMsg);
    }

    // 3) 兜底异常：系统错误
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("未知异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.code(), ErrorCode.SYSTEM_ERROR.msg());
    }
}
