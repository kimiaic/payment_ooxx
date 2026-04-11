package com.oopay.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 请求日志过滤器
 * 记录所有请求的处理时间和相关信息
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter implements WebFilter {

    private static final String REQUEST_START_TIME = "requestStartTime";
    private static final String REQUEST_ID = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        exchange.getAttributes().put(REQUEST_ID, requestId);
        exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());

        // 获取请求信息
        String method = exchange.getRequest().getMethodValue();
        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);

        // 记录请求开始
        log.info("[{}] Request started: {} {} from {}", requestId, method, path, clientIp);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    // 计算处理时间
                    Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
                    Duration duration = Duration.between(startTime, Instant.now());
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("[{}] Request completed: {} {} - {} in {}ms",
                            requestId, method, path, statusCode, duration.toMillis());
                });
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        // 处理X-Forwarded-For多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
