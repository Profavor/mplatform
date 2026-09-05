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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.classification.domain_system.service.FieldEncryptionService encryptionService;

    public void setEncryptionService(com.classification.domain_system.service.FieldEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void executeWebService(String configJson, String payloadJson) throws Exception {
        JsonNode config = objectMapper.readTree(configJson);
        String url = config.has("url") ? config.get("url").asText() : "";
        String methodStr = config.has("method") ? config.get("method").asText() : "POST";
        
        if (url.isBlank()) {
            log.warn("WEB_SERVICE execution skipped: URL is blank");
            return;
        }

        // Validate URL against SSRF (blocks cloud metadata, loopback, private IPs)
        com.classification.domain_system.security.UrlSecurityValidator.validateExternalUrl(url);

        HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Parse custom headers
        if (config.has("headers") && config.get("headers").isArray()) {
            for (JsonNode headerNode : config.get("headers")) {
                String key = headerNode.has("key") ? headerNode.get("key").asText() : "";
                String value = headerNode.has("value") ? headerNode.get("value").asText() : "";
                if (!key.isBlank()) {
                    if (encryptionService != null && encryptionService.isEncrypted(value)) {
                        value = encryptionService.decrypt(value);
                    }
                    headers.add(key, value);
                }
            }
        }

        // Auto-detect and inject Idempotency-Key if provided in payload and not already specified in custom headers (#154)
        if (headers.getFirst("Idempotency-Key") == null && payloadJson != null && !payloadJson.isBlank()) {
            try {
                JsonNode payloadNode = objectMapper.readTree(payloadJson);
                if (payloadNode.has("idempotencyKey") && !payloadNode.get("idempotencyKey").isNull() && !payloadNode.get("idempotencyKey").asText().isBlank()) {
                    headers.set("Idempotency-Key", payloadNode.get("idempotencyKey").asText());
                } else if (payloadNode.has("externalId") && !payloadNode.get("externalId").isNull() && !payloadNode.get("externalId").asText().isBlank()) {
                    headers.set("Idempotency-Key", payloadNode.get("externalId").asText());
                }
            } catch (Exception ignored) {}
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
