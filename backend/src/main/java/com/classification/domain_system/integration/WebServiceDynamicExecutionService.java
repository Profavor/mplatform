package com.classification.domain_system.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Service
public class WebServiceDynamicExecutionService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @Value("${integration.webservice.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${integration.webservice.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restTemplate = new RestTemplate(factory);
    }

    public void executeWebService(String configJson, String payloadJson) throws Exception {
        JsonNode config = objectMapper.readTree(configJson);
        String url = config.has("url") ? config.get("url").asText() : "";
        String methodStr = config.has("method") ? config.get("method").asText() : "POST";
        
        if (url.isBlank()) {
            log.warn("WEB_SERVICE execution skipped: URL is blank");
            return;
        }

        HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Parse custom headers
        if (config.has("headers") && config.get("headers").isArray()) {
            for (JsonNode headerNode : config.get("headers")) {
                String key = headerNode.get("key").asText();
                String value = headerNode.get("value").asText();
                if (!key.isBlank()) {
                    headers.add(key, value);
                }
            }
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(payloadJson, headers);

        log.info("Executing WEB_SERVICE request to URL: {} with Method: {}", url, method);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
            log.info("WEB_SERVICE response status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("WEB_SERVICE execution failed: {}", e.getMessage());
            throw new RuntimeException("WEB_SERVICE execution failed: " + e.getMessage(), e);
        }
    }
}
