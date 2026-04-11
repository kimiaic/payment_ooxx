package com.oopay.common.util;

import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;

/**
 * 商户请求签名专用工具类
 */
public class SignUtil {

    private static final int NONCE_LENGTH = 16;
    private static final int DEFAULT_TOLERANCE_SECONDS = 300; // 5分钟容差

    /**
     * 构建待签名字符串
     *
     * @param params 参数Map
     * @return 待签名字符串（key1=value1\u0026key2=value2...）
     */
    public static String buildSignStr(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        // 过滤空值和 sign 字段，按键排序
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

    /**
     * 生成随机 Nonce 字符串
     *
     * @return 16位随机字符串（字母数字混合）
     */
    public static String generateNonce() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(NONCE_LENGTH);
        for (int i = 0; i < NONCE_LENGTH; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 校验时间戳是否过期（防重放攻击）
     *
     * @param timestamp        请求时间戳（秒级 Unix 时间戳）
     * @param toleranceSeconds 容差时间（秒）
     * @return true-未过期，false-已过期或未来时间
     */
    public static boolean isExpired(long timestamp, int toleranceSeconds) {
        long now = System.currentTimeMillis() / 1000;
        // 容差时间内有效
        return Math.abs(now - timestamp) <= toleranceSeconds;
    }

    /**
     * 校验时间戳是否过期（使用默认容差5分钟）
     *
     * @param timestamp 请求时间戳（秒级）
     * @return true-未过期
     */
    public static boolean isExpired(long timestamp) {
        return isExpired(timestamp, DEFAULT_TOLERANCE_SECONDS);
    }
}
