package com.oopay.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("PENDING", "待支付"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "支付成功"),
    FAILED("FAILED", "支付失败"),
    CLOSED("CLOSED", "已关闭"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String desc;

    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
