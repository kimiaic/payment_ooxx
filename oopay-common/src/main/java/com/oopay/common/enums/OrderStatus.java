package com.oopay.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {

    /**
     * 待支付
     */
    PENDING(0, "待支付"),

    /**
     * 支付中
     */
    PAYING(1, "支付中"),

    /**
     * 支付成功
     */
    SUCCESS(2, "支付成功"),

    /**
     * 支付失败
     */
    FAILED(3, "支付失败"),

    /**
     * 已关闭
     */
    CLOSED(4, "已关闭"),

    /**
     * 已取消
     */
    CANCELLED(5, "已取消"),

    /**
     * 部分退款
     */
    PARTIAL_REFUND(6, "部分退款"),

    /**
     * 全额退款
     */
    FULL_REFUND(7, "全额退款");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
