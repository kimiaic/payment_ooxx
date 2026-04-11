package com.oopay.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额转换工具类
 * 处理分与元之间的转换
 */
public class AmountUtil {

    /**
     * 元转分
     *
     * @param yuan 金额（元）
     * @return 金额（分）
     */
    public static Long yuanToFen(String yuan) {
        if (yuan == null || yuan.isEmpty()) {
            return null;
        }
        BigDecimal amount = new BigDecimal(yuan);
        return amount.multiply(new BigDecimal("100")).longValue();
    }

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
     * @return 金额（元）
     */
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return null;
        }
        return new BigDecimal(fen).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * 分转元（字符串）
     *
     * @param fen 金额（分）
     * @return 金额（元）字符串
     */
    public static String fenToYuanStr(Long fen) {
        BigDecimal yuan = fenToYuan(fen);
        return yuan == null ? null : yuan.toString();
    }

    /**
     * 校验金额是否合法
     *
     * @param amount 金额（分）
     * @return 是否合法
     */
    public static boolean isValidAmount(Long amount) {
        return amount != null && amount > 0;
    }

    /**
     * 校验金额范围
     *
     * @param amount 金额（分）
     * @param min    最小金额（分）
     * @param max    最大金额（分）
     * @return 是否在范围内
     */
    public static boolean isInRange(Long amount, Long min, Long max) {
        if (amount == null) {
            return false;
        }
        return amount >= min && amount <= max;
    }

    /**
     * 计算手续费
     *
     * @param amount   金额（分）
     * @param rate     费率（如0.006表示0.6%）
     * @param minFee   最低手续费（分）
     * @param maxFee   最高手续费（分）
     * @return 手续费（分）
     */
    public static Long calculateFee(Long amount, BigDecimal rate, Long minFee, Long maxFee) {
        if (amount == null || rate == null) {
            return null;
        }
        BigDecimal amountDecimal = new BigDecimal(amount);
        BigDecimal feeDecimal = amountDecimal.multiply(rate).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        long fee = feeDecimal.longValue();

        if (minFee != null && fee < minFee) {
            fee = minFee;
        }
        if (maxFee != null && fee > maxFee) {
            fee = maxFee;
        }
        return fee;
    }
}
