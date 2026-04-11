package com.oopay.common.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatus {

    PENDING("PENDING", "待退款"),
    PROCESSING("PROCESSING", "退款中"),
    SUCCESS("SUCCESS", "退款成功"),
    FAILED("FAILED", "退款失败");

    private final String code;
    private final String desc;

    RefundStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
