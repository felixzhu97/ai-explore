package com.ai.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory rate limiter using a sliding window counter.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStarts = new ConcurrentHashMap<>();

    public RateLimitInterceptor(int maxRequestsPerMinute) {
        this.maxRequests = maxRequestsPerMinute;
        this.windowMs = 60_000;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String key = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Long windowStart = windowStarts.get(key);

        if (windowStart == null || now - windowStart > windowMs) {
            windowStarts.put(key, now);
            counters.computeIfAbsent(key, k -> new AtomicLong()).set(0);
        }

        long count = counters.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

        if (count > maxRequests) {
            log.warn("Rate limit exceeded for {}: {} requests in window", key, count);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return false;
        }

        return true;
    }
}
