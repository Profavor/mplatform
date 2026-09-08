package com.classification.domain_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Profile("test")
@Slf4j
public class LocalDistributedLockService implements DistributedLockService {

    private final ConcurrentMap<String, Long> localLocks = new ConcurrentHashMap<>();

    @Override
    public boolean acquireLock(String lockKey, Duration lockTimeout) {
        long now = System.currentTimeMillis();
        long expireAt = now + (lockTimeout != null ? lockTimeout.toMillis() : 60000);

        Long currentExpire = localLocks.compute(lockKey, (k, v) -> {
            if (v == null || v < now) {
                return expireAt;
            }
            return v;
        });

        boolean acquired = (currentExpire != null && currentExpire == expireAt);
        log.info("[LocalDistributedLock] {} lock for key: '{}'", acquired ? "Acquired" : "Could not acquire", lockKey);
        return acquired;
    }

    @Override
    public void releaseLock(String lockKey) {
        localLocks.remove(lockKey);
        log.info("[LocalDistributedLock] Released lock for key: '{}'", lockKey);
    }
}
