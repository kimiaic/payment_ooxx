package com.oopay.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class HmacSha256Util {
    private static final String ALGORITHM = "HmacSHA256";

    public static String sign(Map<String, String> params, String secretKey) {
        if (params == null || secretKey == null) {
            throw new IllegalArgumentException("参数和密钥不能为空");
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

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 签名失败", e);
        }
    }

    public static boolean verify(Map<String, String> params, String secretKey, String sign) {
        if (sign == null || sign.isEmpty()) {
            return false;
        }
        String calculatedSign = sign(params, secretKey);
        return calculatedSign.equalsIgnoreCase(sign);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
