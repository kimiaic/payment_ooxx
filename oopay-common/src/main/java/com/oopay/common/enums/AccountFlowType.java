package com.oopay.common.enums;

import lombok.Getter;

/**
 * 账户流水类型枚举
 */
@Getter
public enum AccountFlowType {

    /**
     * 收入-支付订单
     */
    INCOME_PAY(1, "收入-支付订单"),

    /**
     * 收入-充值
     */
    INCOME_RECHARGE(2, "收入-充值"),

    /**
     * 支出-提现
     */
    EXPENSE_WITHDRAW(3, "支出-提现"),

    /**
     * 支出-手续费
     */
    EXPENSE_FEE(4, "支出-手续费"),

    /**
     * 冻结
     */
    FREEZE(5, "冻结"),

    /**
     * 解冻
     */
    UNFREEZE(6, "解冻"),

    /**
     * 退款
     */
    REFUND(7, "退款"),

    /**
     * 调账
     */
    ADJUST(8, "调账"),

    /**
     * 其他
     */
    OTHER(99, "其他");

    private final Integer code;
    private final String desc;

    AccountFlowType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AccountFlowType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (AccountFlowType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为收入类型
     */
    public boolean isIncome() {
        return this == INCOME_PAY || this == INCOME_RECHARGE;
    }

    /**
     * 是否为支出类型
     */
    public boolean isExpense() {
        return this == EXPENSE_WITHDRAW || this == EXPENSE_FEE;
    }
}
