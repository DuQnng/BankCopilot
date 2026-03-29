package com.idea.common;

public enum ErrorCode {

    // 通用
    PARAM_INVALID(1001, "参数不合法"),
    UNAUTHORIZED(1002, "未登录或登录已过期"),
    FORBIDDEN(1003, "无权限"),
    LOGIN_FAILED(1101, "用户名或密码错误"),
    PHONE_INVALID(1102, "手机号格式不正确"),
    CODE_INVALID(1103, "验证码错误或已过期"),
    USERNAME_EXISTS(1104, "用户名已存在"),
    REGISTER_FAILED(1105, "注册失败"),
    SYSTEM_ERROR(9999, "系统繁忙，请稍后再试"),


    // 账户/交易（你项目核心）
    ACCOUNT_NOT_FOUND(2001, "账户不存在"),
    ACCOUNT_FROZEN(2002, "账户状态异常"),
    INSUFFICIENT_BALANCE(2003, "余额不足"),
    TRANSFER_TO_SELF(2004, "不能给自己转账");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() { return code; }
    public String msg() { return msg; }
}
