package com.oopay.gateway.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 限流配置
 * 基于Redis的令牌桶限流
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "oopay.rate-limit")
@Data
public class RateLimitConfig {

    /**
     * 是否启用限流
     */
    private Boolean enabled = true;

    /**
     * 每秒允许的请求数（令牌桶填充速率）
     */
    private Integer replenishRate = 100;

    /**
     * 令牌桶容量（突发流量缓冲）
     */
    private Integer burstCapacity = 200;

    /**
     * 限流Key解析器（按IP限流）
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
                    : "unknown";
            String path = exchange.getRequest().getPath().value();
            return Mono.just(ip + ":" + path);
        };
    }

    /**
     * Redis限流器（全局默认）
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(replenishRate, burstCapacity);
    }

    /**
     * 针对支付接口的限流器（更严格）
     */
    @Bean(name = "paymentRateLimiter")
    public RedisRateLimiter paymentRateLimiter() {
        // 支付接口：每秒20请求，突发40
        return new RedisRateLimiter(20, 40);
    }

    /**
     * 针对查询接口的限流器（较宽松）
     */
    @Bean(name = "queryRateLimiter")
    public RedisRateLimiter queryRateLimiter() {
        // 查询接口：每秒200请求，突发400
        return new RedisRateLimiter(200, 400);
    }
}
