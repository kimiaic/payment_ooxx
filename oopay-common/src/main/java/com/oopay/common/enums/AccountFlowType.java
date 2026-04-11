package com.oopay.common.enums;

import lombok.Getter;

/**
 * 账户流水交易类型枚举
 */
@Getter
public enum AccountFlowType {

    RECHARGE(1, "充值"),
    WITHDRAW(2, "提现"),
    PAYMENT(3, "支付"),
    REFUND(4, "退款"),
    FEE(5, "手续费");

    private final int code;
    private final String desc;

    AccountFlowType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
