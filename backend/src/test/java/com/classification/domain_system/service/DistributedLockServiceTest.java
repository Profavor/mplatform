package com.classification.domain_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class DistributedLockServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisDistributedLockService redisLockService;
    private LocalDistributedLockService localLockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        redisLockService = new RedisDistributedLockService(redisTemplate);
        localLockService = new LocalDistributedLockService();
    }

    @Test
    @DisplayName("Redis 락 획득 성공 시 true 반환 검증")
    void redisLock_WhenAcquired_ShouldReturnTrue() {
        String key = "test-key";
        Duration timeout = Duration.ofMinutes(5);
        given(valueOperations.setIfAbsent(eq("lock:" + key), any(), eq(timeout))).willReturn(Boolean.TRUE);

        boolean acquired = redisLockService.acquireLock(key, timeout);

        assertThat(acquired).isTrue();
    }

    @Test
    @DisplayName("Redis 락 획득 실패 시 (이미 다른 인스턴스가 점유) false 반환 검증")
    void redisLock_WhenAlreadyLocked_ShouldReturnFalse() {
        String key = "test-key";
        Duration timeout = Duration.ofMinutes(5);
        given(valueOperations.setIfAbsent(eq("lock:" + key), any(), eq(timeout))).willReturn(Boolean.FALSE);

        boolean acquired = redisLockService.acquireLock(key, timeout);

        assertThat(acquired).isFalse();
    }

    @Test
    @DisplayName("Redis 락 해제 시 redisTemplate.delete 호출 검증")
    void redisLock_Release_ShouldDeleteKey() {
        String key = "test-key";

        redisLockService.releaseLock(key);

        verify(redisTemplate).delete("lock:" + key);
    }

    @Test
    @DisplayName("Local 락은 동시성 획득 제어 및 해제 정상 작동 검증")
    void localLock_ShouldWorkInMemory() {
        String key = "local-key";
        Duration timeout = Duration.ofSeconds(10);

        boolean first = localLockService.acquireLock(key, timeout);
        boolean second = localLockService.acquireLock(key, timeout);

        assertThat(first).isTrue();
        assertThat(second).isFalse();

        localLockService.releaseLock(key);

        boolean third = localLockService.acquireLock(key, timeout);
        assertThat(third).isTrue();
    }

    @Test
    @DisplayName("Local 락 동시 다중 스레드 경합 시 오직 1개 스레드만 락을 획득해야 함")
    void localLock_ConcurrentAccess_OnlyOneAcquires() throws Exception {
        String key = "concurrent-key";
        Duration timeout = Duration.ofSeconds(10);
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    if (localLockService.acquireLock(key, timeout)) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                }
            }));
        }

        latch.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
    }
}

