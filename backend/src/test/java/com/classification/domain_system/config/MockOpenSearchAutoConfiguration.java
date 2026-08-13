package com.classification.domain_system.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.classification.domain_system.repository.RecordSearchRepository;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@AutoConfiguration
public class MockOpenSearchAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticsearchOperations elasticsearchOperations() {
        return Mockito.mock(ElasticsearchOperations.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RecordSearchRepository recordSearchRepository() {
        return Mockito.mock(RecordSearchRepository.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }
}
