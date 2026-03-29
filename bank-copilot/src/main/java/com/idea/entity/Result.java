package com.idea.entity;

import lombok.Data;

@Data
public class Result {

    private Integer code;     // 1成功，0失败
    private Integer bizCode;  // 业务错误码，成功固定为0
    private String msg;
    private Object data;

    public static Result success() {
        Result r = new Result();
        r.code = 1;
        r.bizCode = 0;
        r.msg = "success";
        return r;
    }

    public static Result success(Object data) {
        Result r = new Result();
        r.code = 1;
        r.bizCode = 0;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static Result error(String msg) {
        Result r = new Result();
        r.code = 0;
        r.bizCode = 1000; // 默认未知业务错误
        r.msg = msg;
        return r;
    }

    public static Result error(Integer bizCode, String msg) {
        Result r = new Result();
        r.code = 0;
        r.bizCode = bizCode;
        r.msg = msg;
        return r;
    }
}
