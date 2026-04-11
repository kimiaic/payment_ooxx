package com.oopay.common.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
public enum PayType {

    WECHAT_NATIVE("WECHAT_NATIVE", "微信支付-扫码"),
    WECHAT_JSAPI("WECHAT_JSAPI", "微信支付-公众号"),
    ALIPAY_QR("ALIPAY_QR", "支付宝-扫码"),
    ALIPAY_WAP("ALIPAY_WAP", "支付宝-手机"),
    USDT_TRC20("USDT_TRC20", "USDT-TRC20");

    private final String code;
    private final String desc;

    PayType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayType fromCode(String code) {
        for (PayType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
