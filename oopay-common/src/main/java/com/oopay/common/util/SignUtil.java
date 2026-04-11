package com.oopay.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商户签名工具类
 * 用于验证商户请求的签名
 */
public class SignUtil {

    /**
     * 生成签名
     *
     * @param params 参数Map（不包含sign字段）
     * @param key    商户密钥
     * @return 签名
     */
    public static String sign(Map<String, Object> params, String key) {
        // 过滤空值和sign字段，按key排序
        String signContent = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> !"sign".equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\u0026"));

        // 拼接密钥
        String signString = signContent + "\u0026key=" + key;

        // 使用HMAC-SHA256签名
        return HmacSha256Util.sign(signString, key);
    }

    /**
     * 验证签名
     *
     * @param params 参数Map（包含sign字段）
     * @param key    商户密钥
     * @return 验证结果
     */
    public static boolean verify(Map<String, Object> params, String key) {
        String sign = (String) params.get("sign");
        if (sign == null || sign.isEmpty()) {
            return false;
        }
        String expectedSign = sign(params, key);
        return sign.equals(expectedSign);
    }

    /**
     * 生成待签名字符串（用于日志或调试）
     *
     * @param params 参数Map
     * @param key    商户密钥
     * @return 待签名字符串
     */
    public static String getSignContent(Map<String, Object> params, String key) {
        String signContent = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> !"sign".equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\u0026"));
        return signContent + "\u0026key=" + key;
    }
}
