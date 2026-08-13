package com.classification.domain_system.config;

import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@Profile("!test")
@EnableElasticsearchRepositories(basePackages = "com.classification.domain_system.repository")
public class OpenSearchConfig extends AbstractOpenSearchConfiguration {

    @Value("${spring.opensearch.uris}")
    private String opensearchUris;

    @Override
    public RestHighLevelClient opensearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(opensearchUris.replace("http://", "").replace("https://", ""))
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
