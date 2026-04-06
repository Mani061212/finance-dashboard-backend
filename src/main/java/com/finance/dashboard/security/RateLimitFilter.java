package com.finance.dashboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiter: 100 requests per minute (simple token bucket).
 *
 * NOT annotated with @Component deliberately — if both @Component and
 * SecurityConfig.addFilterBefore() register this filter, it executes TWICE
 * per request. By omitting @Component and registering only in SecurityConfig,
 * exactly one execution per request is guaranteed.
 *
 * NOTE: Uses in-memory storage — state is NOT shared across multiple
 * application instances. For multi-node deployments, use a distributed
 * cache (Redis, Hazelcast).
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MILLIS = 60_000L; // 1 minute

    private final Map<String, RateLimitBucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Simple token bucket (in-memory).
     */
    private static class RateLimitBucket {
        int tokens;
        long lastRefillTime;

        RateLimitBucket() {
            this.tokens = MAX_REQUESTS;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(int requestedTokens) {
            long now = System.currentTimeMillis();
            long elapsedMillis = now - lastRefillTime;

            // Refill tokens based on elapsed time
            if (elapsedMillis >= WINDOW_MILLIS) {
                tokens = MAX_REQUESTS;
                lastRefillTime = now;
            } else {
                // Partial refill: proportional to elapsed time
                long tokensToAdd = (long) (MAX_REQUESTS * elapsedMillis / (double) WINDOW_MILLIS);
                tokens = Math.min(MAX_REQUESTS, (int) (tokens + tokensToAdd));
                lastRefillTime = now;
            }

            if (tokens >= requestedTokens) {
                tokens -= requestedTokens;
                return true;
            }
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        RateLimitBucket bucket = bucketCache.computeIfAbsent(clientIp, ip -> new RateLimitBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                    "\"message\":\"Rate limit exceeded. Max 100 requests/min.\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}