package com.idea.exception;

import com.idea.common.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int bizCode;

    public BusinessException(int bizCode, String message) {
        super(message);
        this.bizCode = bizCode;
    }

    public static BusinessException of(ErrorCode code) {
        return new BusinessException(code.code(), code.msg());
    }

    public static BusinessException of(ErrorCode code, String message) {
        return new BusinessException(code.code(), message);
    }
}
