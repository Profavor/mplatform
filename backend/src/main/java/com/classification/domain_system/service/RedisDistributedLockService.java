package com.classification.domain_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockService implements DistributedLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_KEY_PREFIX = "lock:";

    @Override
    public boolean acquireLock(String lockKey, Duration lockTimeout) {
        String fullKey = LOCK_KEY_PREFIX + lockKey;
        try {
            long threadId = Thread.currentThread().getId();
            String value = String.format("locked_at_%d_by_thread_%d", System.currentTimeMillis(), threadId);
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(fullKey, value, lockTimeout);
            boolean result = Boolean.TRUE.equals(acquired);
            if (result) {
                log.info("[RedisDistributedLock] Successfully acquired lock for key: '{}' with TTL: {}", fullKey, lockTimeout);
            } else {
                log.info("[RedisDistributedLock] Lock already held by another instance for key: '{}'", fullKey);
            }
            return result;
        } catch (Exception e) {
            log.error("[RedisDistributedLock] Failed to communicate with Redis for lock '{}': {}", fullKey, e.getMessage(), e);
            // In case of Redis failure, fallback to false to avoid concurrent execution hazard
            return false;
        }
    }

    @Override
    public void releaseLock(String lockKey) {
        String fullKey = LOCK_KEY_PREFIX + lockKey;
        try {
            redisTemplate.delete(fullKey);
            log.info("[RedisDistributedLock] Released lock for key: '{}'", fullKey);
        } catch (Exception e) {
            log.warn("[RedisDistributedLock] Failed to release lock for key '{}': {}", fullKey, e.getMessage());
        }
    }
}
