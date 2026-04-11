package com.oopay.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求日志过滤器
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID_KEY = "traceId";
    private static final int TRACE_ID_LENGTH = 8;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 生成 traceId
        String traceId = UUID.randomUUID().toString().substring(0, TRACE_ID_LENGTH);
        MDC.put(TRACE_ID_KEY, traceId);

        // 将 traceId 添加到请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(TRACE_ID_KEY, traceId)
                .build();

        long startTime = System.currentTimeMillis();

        String method = request.getMethodValue();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> {
                    long cost = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("[{}] {} {} from {} cost {}ms status {}",
                            traceId, method, path, clientIp, cost, statusCode);

                    MDC.remove(TRACE_ID_KEY);
                });
    }

    @Override
    public int getOrder() {
        // 最高优先级
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        // 如果有多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
