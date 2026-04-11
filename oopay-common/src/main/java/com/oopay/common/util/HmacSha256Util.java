package com.oopay.common.util;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HMAC-SHA256 签名工具类
 */
public class HmacSha256Util {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * 生成签名
     *
     * @param params    参数Map
     * @param secretKey 密钥
     * @return 十六进制签名字符串
     */
    public static String sign(Map<String, String> params, String secretKey) {
        if (params == null || secretKey == null) {
            throw new IllegalArgumentException("参数和密钥不能为空");
        }

        // 1. 过滤 sign 字段并排序
        Map<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && !"sign".equals(key) && value != null && !value.isEmpty()) {
                sortedParams.put(key, value);
            }
        }

        // 2. 构建待签名字符串
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\u0026");
        }
        // 删除最后一个 &
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        // 3. HMAC-SHA256 签名
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

    /**
     * 验证签名
     *
     * @param params    参数Map
     * @param secretKey 密钥
     * @param sign      待验证的签名
     * @return true-验证通过
     */
    public static boolean verify(Map<String, String> params, String secretKey, String sign) {
        if (sign == null || sign.isEmpty()) {
            return false;
        }
        String calculatedSign = sign(params, secretKey);
        return calculatedSign.equalsIgnoreCase(sign);
    }

    /**
     * 字节数组转十六进制字符串
     */
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
