package com.oopay.common.enums;

import lombok.Getter;

@Getter
public enum NotifyStatus {
    PENDING(0, "待通知"),
    SUCCESS(1, "通知成功"),
    FAILED(2, "通知失败");

    private final int code;
    private final String desc;

    NotifyStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
