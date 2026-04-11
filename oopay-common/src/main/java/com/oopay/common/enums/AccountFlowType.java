package com.oopay.common.enums;

import lombok.Getter;

@Getter
public enum AccountFlowType {
    IN(1, "收入"),
    OUT(2, "支出"),
    FREEZE(3, "冻结"),
    UNFREEZE(4, "解冻");

    private final int code;
    private final String desc;

    AccountFlowType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
