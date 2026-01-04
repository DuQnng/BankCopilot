package com.idea.util;

public class LoginContext {
    // 使用 ThreadLocal 保证每个线程独立存储当前用户ID

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    // 设置当前用户ID
    public static void setCurrentUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    // 获取当前用户ID
    public static Long getCurrentUserId() {
        return CURRENT_USER.get();
    }

    // 清理线程变量，避免内存泄漏
    public static void clear() {
        CURRENT_USER.remove();
    }
}
