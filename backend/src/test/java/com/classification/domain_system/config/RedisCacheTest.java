package com.classification.domain_system.config;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisCacheTest {

    @TestConfiguration
    @EnableCaching
    static class TestCacheConfig {
        @Bean
        @Primary
        public CacheManager testCacheManager() {
            return new ConcurrentMapCacheManager("domains");
        }
    }

    @Autowired
    private DomainService domainService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private DomainRepository domainRepository;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("domains") != null) {
            cacheManager.getCache("domains").clear();
        }
    }

    @Test
    void testDomainCaching() {
        UUID domainId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "테스트도메인"));

        when(domainRepository.findById(eq(domainId))).thenReturn(Optional.of(domain));

        // First call - should query repository
        Domain firstResult = domainService.getDomainById(domainId);
        assertNotNull(firstResult);
        assertEquals(domainId, firstResult.getId());
        verify(domainRepository, times(1)).findById(eq(domainId));

        // Second call - should hit cache and NOT query repository again
        Domain secondResult = domainService.getDomainById(domainId);
        assertNotNull(secondResult);
        assertEquals(domainId, secondResult.getId());
        verify(domainRepository, times(1)).findById(eq(domainId));
    }
}
