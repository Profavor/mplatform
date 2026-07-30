package com.classification.domain_system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis가 없는 환경(dev 등)에서 사용되는 인메모리 캐시 설정.
 * RedisCacheConfig의 CacheManager가 생성되지 않을 때 폴백으로 활성화됩니다.
 */
@Configuration
public class LocalCacheConfig {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        // 인자를 전달하지 않으면 요청 시점에 동적으로 캐시를 생성합니다.
        return new ConcurrentMapCacheManager();
    }
}
