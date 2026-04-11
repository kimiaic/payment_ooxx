package com.oopay.common.enums;

import lombok.Getter;

/**
 * 通知状态枚举
 */
@Getter
public enum NotifyStatus {

    /**
     * 等待通知
     */
    PENDING(0, "等待通知"),

    /**
     * 通知成功
     */
    SUCCESS(1, "通知成功"),

    /**
     * 通知失败
     */
    FAILED(2, "通知失败"),

    /**
     * 通知中
     */
    NOTIFYING(3, "通知中"),

    /**
     * 超过重试次数
     */
    EXHAUSTED(4, "超过重试次数");

    private final Integer code;
    private final String desc;

    NotifyStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotifyStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotifyStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否需要重试
     */
    public boolean needRetry() {
        return this == FAILED || this == PENDING;
    }
}
