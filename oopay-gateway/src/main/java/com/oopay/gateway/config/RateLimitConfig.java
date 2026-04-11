package com.oopay.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 限流配置
 */
@Configuration
public class RateLimitConfig {

    /**
     * 默认限流配置：每秒100个请求，burst=200
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(100, 200);
    }

    /**
     * 基于 IP 的限流 Key 解析器
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown"
        );
    }

    /**
     * 基于 API Key 的限流 Key 解析器
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-API-Key") != null
                        ? exchange.getRequest().getHeaders().getFirst("X-API-Key")
                        : "anonymous"
        );
    }
}
