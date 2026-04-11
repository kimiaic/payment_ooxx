package com.oopay.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额转换工具类
 */
public class AmountUtil {

    private static final int SCALE = 2;
    private static final long MAX_AMOUNT_FEN = 10000000000L; // 1亿元（分）

    /**
     * 元转分
     *
     * @param yuan 金额（元）
     * @return 金额（分）
     */
    public static Long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return null;
        }
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 分转元
     *
     * @param fen 金额（分）
     * @return 金额（元），保留2位小数
     */
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return null;
        }
        return new BigDecimal(fen).divide(new BigDecimal("100"), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 校验金额是否合法
     *
     * @param fen 金额（分）
     * @return true-合法
     */
    public static boolean validateAmount(Long fen) {
        if (fen == null) {
            return false;
        }
        return fen > 0 && fen <= MAX_AMOUNT_FEN;
    }

    /**
     * 校验金额，非法时抛出异常
     *
     * @param fen 金额（分）
     */
    public static void validateAmountOrThrow(Long fen) {
        if (!validateAmount(fen)) {
            throw new IllegalArgumentException("金额不合法，必须在 0-" + MAX_AMOUNT_FEN + " 分之间");
        }
    }
}
