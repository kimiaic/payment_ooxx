package com.oopay.common.enums;

import lombok.Getter;

@Getter
public enum PayType {
    WECHAT("WECHAT", "微信支付"),
    ALIPAY("ALIPAY", "支付宝"),
    USDT("USDT", "USDT");

    private final String code;
    private final String desc;

    PayType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
