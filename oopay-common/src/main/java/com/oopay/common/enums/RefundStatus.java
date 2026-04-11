package com.oopay.common.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatus {

    /**
     * 退款申请中
     */
    APPLYING(0, "退款申请中"),

    /**
     * 退款中
     */
    PROCESSING(1, "退款中"),

    /**
     * 退款成功
     */
    SUCCESS(2, "退款成功"),

    /**
     * 退款失败
     */
    FAILED(3, "退款失败"),

    /**
     * 退款关闭
     */
    CLOSED(4, "退款关闭");

    private final Integer code;
    private final String desc;

    RefundStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
