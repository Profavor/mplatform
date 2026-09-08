package com.classification.domain_system.service;

import java.time.Duration;

public interface DistributedLockService {

    /**
     * Attempts to acquire a distributed lock for the given key with a specified timeout.
     *
     * @param lockKey the unique lock key
     * @param lockTimeout duration after which the lock automatically expires
     * @return true if lock was acquired, false otherwise
     */
    boolean acquireLock(String lockKey, Duration lockTimeout);

    /**
     * Releases the lock for the given key if held.
     *
     * @param lockKey the unique lock key
     */
    void releaseLock(String lockKey);
}
