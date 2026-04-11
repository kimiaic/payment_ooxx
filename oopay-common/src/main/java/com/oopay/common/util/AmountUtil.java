package com.oopay.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountUtil {
    private static final int SCALE = 2;
    private static final long MAX_AMOUNT_FEN = 10000000000L;

    public static Long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return null;
        }
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return null;
        }
        return new BigDecimal(fen).divide(new BigDecimal("100"), SCALE, RoundingMode.HALF_UP);
    }

    public static boolean validateAmount(Long fen) {
        if (fen == null) {
            return false;
        }
        return fen > 0 && fen <= MAX_AMOUNT_FEN;
    }
}
