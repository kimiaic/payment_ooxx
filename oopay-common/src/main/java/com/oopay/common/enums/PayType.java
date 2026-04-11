package com.oopay.common.enums;

import lombok.Getter;

/**
 * 支付类型枚举
 */
@Getter
public enum PayType {

    /**
     * 支付宝
     */
    ALIPAY(1, "支付宝"),

    /**
     * 微信支付
     */
    WECHAT(2, "微信支付"),

    /**
     * 银联
     */
    UNIONPAY(3, "银联"),

    /**
     * 数字货币
     */
    CRYPTO(4, "数字货币"),

    /**
     * 话费充值
     */
    PHONE(5, "话费充值"),

    /**
     * 电费充值
     */
    ELECTRIC(6, "电费充值"),

    /**
     * 油卡充值
     */
    OIL(7, "油卡充值"),

    /**
     * 信用卡代付
     */
    CREDIT_CARD(8, "信用卡代付"),

    /**
     * 其他
     */
    OTHER(99, "其他");

    private final Integer code;
    private final String desc;

    PayType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
