package com.devlens.ai.service;

import com.devlens.common.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding window rate limiter for DevLens AI endpoints
 */
@Component
public class AiRateLimiter {

    private final int maxRequestsPerMinute;
    private final Map<Long, Deque<Long>> userRequestTimestamps = new ConcurrentHashMap<>();

    public AiRateLimiter(@Value("${devlens.ai.rate-limit.requests-per-minute:40}") int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    /**
     * Checks and records a request for the given user. Throws RateLimitExceededException if exceeded.
     */
    public synchronized void checkLimit(Long userId) {
        if (userId == null) return;

        long now = System.currentTimeMillis();
        long windowStart = now - 60_000L; // 1 minute window

        Deque<Long> timestamps = userRequestTimestamps.computeIfAbsent(userId, k -> new ArrayDeque<>());

        // Evict expired timestamps
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequestsPerMinute) {
            throw new RateLimitExceededException("Rate limit exceeded. Maximum " + maxRequestsPerMinute + " AI requests per minute allowed.");
        }

        timestamps.addLast(now);
    }
}
