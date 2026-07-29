package com.classification.domain_system.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class LocalCacheConfigTest {

    @Test
    @DisplayName("LocalCacheConfig는 ConcurrentMapCacheManager를 생성하고 effectiveFields 캐시를 포함한다")
    void cacheManager_ReturnsConcurrentMapCacheManager() {
        LocalCacheConfig config = new LocalCacheConfig();
        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);
        assertThat(cacheManager.getCache("effectiveFields")).isNotNull();
    }
}
