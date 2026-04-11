package com.oopay.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HMAC-SHA256 签名工具类
 */
public class HmacSha256Util {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 生成 HMAC-SHA256 签名（Base64编码）
     *
     * @param data 待签名数据
     * @param key  密钥
     * @return Base64编码的签名
     */
    public static String sign(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }

    /**
     * 生成 HMAC-SHA256 签名（Hex编码）
     *
     * @param data 待签名数据
     * @param key  密钥
     * @return Hex编码的签名
     */
    public static String signHex(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }

    /**
     * 验证签名
     *
     * @param data      原始数据
     * @param key       密钥
     * @param signature 待验证的签名
     * @return 是否验证通过
     */
    public static boolean verify(String data, String key, String signature) {
        String expected = sign(data, key);
        return expected.equals(signature);
    }

    /**
     * 字节数组转Hex字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
