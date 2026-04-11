package com.oopay.common.util;

import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;

public class SignUtil {
    private static final int NONCE_LENGTH = 16;
    private static final int DEFAULT_TOLERANCE_SECONDS = 300;

    public static String buildSignStr(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        Map<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && !"sign".equals(key) && value != null && !value.isEmpty()) {
                sortedParams.put(key, value);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\u0026");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public static String generateNonce() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(NONCE_LENGTH);
        for (int i = 0; i < NONCE_LENGTH; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean isExpired(long timestamp) {
        return isExpired(timestamp, DEFAULT_TOLERANCE_SECONDS);
    }

    public static boolean isExpired(long timestamp, int toleranceSeconds) {
        long now = System.currentTimeMillis() / 1000;
        return Math.abs(now - timestamp) > toleranceSeconds;
    }
}
