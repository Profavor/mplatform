package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.security.UrlSecurityValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CartbomOutboundWebhookService {

    @Setter
    private RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Getter
    @Setter
    @Value("${cartbom.ingest.url:https://cartbom.com/api/partners/ingest}")
    private String cartbomIngestUrl = "https://cartbom.com/api/partners/ingest";

    @Getter
    @Setter
    @Value("${cartbom.ingest.source-id:mdm-mplat}")
    private String sourceId = "mdm-mplat";

    @Getter
    @Setter
    @Value("${cartbom.ingest.source-token:90be15c05c0bcadc}")
    private String sourceToken = "90be15c05c0bcadc";

    @Autowired
    public CartbomOutboundWebhookService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 카트봄 웹훅 발송 대상인지 검사
     */
    public boolean shouldDispatch(Record record, String dataJson) {
        if (record == null || record.getNode() == null || record.getNode().getDomain() == null) {
            return false;
        }
        Domain domain = record.getNode().getDomain();
        Map<String, String> domainNameMap = domain.getName();
        if (domainNameMap == null || domainNameMap.isEmpty()) {
            return false;
        }
        boolean isCartbom = domainNameMap.values().stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(v -> v.contains("카트봄") || v.toLowerCase().contains("cartbom"));
        if (!isCartbom) {
            return false;
        }

        if (dataJson == null || dataJson.isBlank()) {
            return false;
        }

        try {
            Map<String, Object> map = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            boolean hasProductId = map.containsKey("PRODUCT_ID") && map.get("PRODUCT_ID") != null && !map.get("PRODUCT_ID").toString().isBlank();
            boolean hasPrice = map.containsKey("PRODUCT_PRICE") && map.get("PRODUCT_PRICE") != null;
            return hasProductId && hasPrice;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 카트봄 파트너스 ingest 규격 페이로드 생성
     */
    public String buildPayload(Record record, String dataJson) throws Exception {
        Map<String, Object> data = (dataJson != null && !dataJson.isBlank())
                ? objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {})
                : new LinkedHashMap<>();

        UUID recordId = record.getId();
        int version = record.getVersion() != null ? record.getVersion() : 1;

        String productId = data.getOrDefault("PRODUCT_ID", "").toString();
        String productName = data.getOrDefault("PRODUCT_NAME", "").toString();
        String categoryName = data.getOrDefault("CATEGORY_NAME", "").toString();

        Object priceObj = data.get("PRODUCT_PRICE");
        Number price = 0;
        if (priceObj instanceof Number) {
            price = (Number) priceObj;
        } else if (priceObj != null) {
            try {
                price = Long.parseLong(priceObj.toString().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }

        String inventoryStatus = data.getOrDefault("INVENTORY_STATUS", "observed").toString();
        String observedAtIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("externalId", "mdm-record-" + recordId + "-" + version);
        payload.put("sourceUrl", "https://mdm.mplat.store/records/" + recordId);

        Map<String, Object> product = new LinkedHashMap<>();
        product.put("retailerId", "coupang");
        product.put("retailerProductNumber", productId);
        product.put("name", productName);
        product.put("category", categoryName);
        payload.put("product", product);

        Map<String, Object> observation = new LinkedHashMap<>();
        observation.put("scope", "online");
        observation.put("scopeEvidence", "mdm-workflow-approved");
        observation.put("observedPrice", price);
        observation.put("observedAt", observedAtIso);
        observation.put("inventoryStatus", inventoryStatus);
        payload.put("observation", observation);

        return objectMapper.writeValueAsString(payload);
    }

    /**
     * 멱등키 생성
     */
    public String buildIdempotencyKey(String productId, Object price, Integer version) {
        return String.format("%s:%s:%s", productId != null ? productId : "UNKNOWN", version != null ? version : 1, price != null ? price : 0);
    }

    /**
     * 동기 전송 (테스트 및 직접 호출용)
     */
    public boolean dispatchSync(Record record, String dataJson) {
        try {
            if (!shouldDispatch(record, dataJson)) {
                log.debug("[CartbomWebhook] Record {} is not eligible for Cartbom webhook dispatch", record != null ? record.getId() : null);
                return false;
            }

            UrlSecurityValidator.validateExternalUrl(cartbomIngestUrl);

            String payload = buildPayload(record, dataJson);
            Map<String, Object> data = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            String productId = data.getOrDefault("PRODUCT_ID", "").toString();
            String idempotencyKey = buildIdempotencyKey(productId, data.get("PRODUCT_PRICE"), record.getVersion());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-cartbom-source-id", sourceId);
            headers.set("x-cartbom-source-token", sourceToken);
            headers.set("Idempotency-Key", idempotencyKey);

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            log.info("[CartbomWebhook] Dispatching verified price to {} for record {} (productId: {}, idempotencyKey: {})",
                    cartbomIngestUrl, record.getId(), productId, idempotencyKey);

            ResponseEntity<String> response = restTemplate.exchange(cartbomIngestUrl, HttpMethod.POST, entity, String.class);
            log.info("[CartbomWebhook] Successfully dispatched to Cartbom. Status: {}", response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("[CartbomWebhook] Failed to dispatch webhook to Cartbom for record {}: {}",
                    record != null ? record.getId() : null, e.getMessage());
            return false;
        }
    }

    /**
     * 비동기 전송 (결재 승인 리스너용)
     */
    @Async
    public void dispatchAsync(Record record, String dataJson) {
        dispatchSync(record, dataJson);
    }
}
