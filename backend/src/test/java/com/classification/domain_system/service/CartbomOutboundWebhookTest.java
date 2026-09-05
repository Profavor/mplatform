package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartbomOutboundWebhookTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CartbomOutboundWebhookService webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new CartbomOutboundWebhookService(objectMapper);
        webhookService.setRestTemplate(restTemplate);
        webhookService.setCartbomIngestUrl("https://cartbom.com/api/partners/ingest");
        webhookService.setSourceId("mdm-mplat");
        webhookService.setSourceToken("test-cartbom-token-123");
    }

    @Test
    @DisplayName("#109: 카트봄 상품 레코드의 데이터로 계약 규격에 맞는 Ingest JSON 페이로드를 생성한다")
    void testBuildPayload() throws Exception {
        UUID recordId = UUID.randomUUID();
        Record record = new Record();
        record.setId(recordId);
        record.setVersion(3);

        Domain domain = new Domain();
        domain.setName(Map.of("ko", "카트봄"));
        ClassificationNode node = new ClassificationNode();
        node.setDomain(domain);
        record.setNode(node);

        String dataJson = """
                {
                    "PRODUCT_ID": "6011227725",
                    "PRODUCT_NAME": "로지텍 G102 게이밍 마우스",
                    "CATEGORY_NAME": "컴퓨터/주변기기",
                    "PRODUCT_PRICE": 24060,
                    "INVENTORY_STATUS": "observed"
                }
                """;

        String payloadJson = webhookService.buildPayload(record, dataJson);
        JsonNode root = objectMapper.readTree(payloadJson);

        assertThat(root.get("externalId").asText()).isEqualTo("mdm-record-" + recordId + "-3");
        assertThat(root.get("sourceUrl").asText()).isEqualTo("https://mdm.mplat.store/records/" + recordId);
        assertThat(root.get("product").get("retailerId").asText()).isEqualTo("coupang");
        assertThat(root.get("product").get("retailerProductNumber").asText()).isEqualTo("6011227725");
        assertThat(root.get("product").get("name").asText()).isEqualTo("로지텍 G102 게이밍 마우스");
        assertThat(root.get("product").get("category").asText()).isEqualTo("컴퓨터/주변기기");
        assertThat(root.get("observation").get("scope").asText()).isEqualTo("online");
        assertThat(root.get("observation").get("scopeEvidence").asText()).isEqualTo("mdm-workflow-approved");
        assertThat(root.get("observation").get("observedPrice").asInt()).isEqualTo(24060);
        assertThat(root.get("observation").get("inventoryStatus").asText()).isEqualTo("observed");
        assertThat(root.get("observation").has("observedAt")).isTrue();
        // Check ISO-8601 UTC format
        String observedAt = root.get("observation").get("observedAt").asText();
        assertThat(Instant.parse(observedAt)).isNotNull();
    }

    @Test
    @DisplayName("#109: 웹훅 발송 시 x-cartbom 헤더와 Idempotency-Key 헤더를 포함하여 POST 전송한다")
    void testDispatchWebhook_HeadersAndPost() {
        UUID recordId = UUID.randomUUID();
        Record record = new Record();
        record.setId(recordId);
        record.setVersion(1);

        Domain domain = new Domain();
        domain.setName(Map.of("ko", "카트봄"));
        ClassificationNode node = new ClassificationNode();
        node.setDomain(domain);
        record.setNode(node);

        String dataJson = """
                {
                    "PRODUCT_ID": "6011227725",
                    "PRODUCT_NAME": "로지텍 G102",
                    "PRODUCT_PRICE": 24060
                }
                """;

        when(restTemplate.exchange(
                eq("https://cartbom.com/api/partners/ingest"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.accepted().body("{\"status\":\"accepted\"}"));

        boolean success = webhookService.dispatchSync(record, dataJson);

        assertThat(success).isTrue();

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).exchange(
                eq("https://cartbom.com/api/partners/ingest"),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(String.class)
        );

        HttpEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getHeaders().getFirst("x-cartbom-source-id")).isEqualTo("mdm-mplat");
        assertThat(capturedEntity.getHeaders().getFirst("x-cartbom-source-token")).isEqualTo("test-cartbom-token-123");
        assertThat(capturedEntity.getHeaders().getFirst("Idempotency-Key")).contains("6011227725");
        assertThat(capturedEntity.getHeaders().getFirst("Content-Type")).contains("application/json");
    }

    @Test
    @DisplayName("#109: 카트봄 상품 도메인이 아니거나 PRODUCT_ID/PRODUCT_PRICE가 없으면 웹훅 발송 대상에서 제외된다")
    void testShouldDispatch_Filters() {
        Record record = new Record();
        Domain domain = new Domain();
        domain.setName(Map.of("ko", "고객도메인"));
        ClassificationNode node = new ClassificationNode();
        node.setDomain(domain);
        record.setNode(node);

        // Not cartbom domain
        assertThat(webhookService.shouldDispatch(record, "{\"CUSTOMER_NAME\":\"홍길동\"}")).isFalse();

        // Cartbom domain but no price
        domain.setName(Map.of("ko", "카트봄"));
        assertThat(webhookService.shouldDispatch(record, "{\"PRODUCT_NAME\":\"상품명만 존재\"}")).isFalse();

        // Cartbom domain with product_id and price -> Should dispatch
        assertThat(webhookService.shouldDispatch(record, "{\"PRODUCT_ID\":\"12345\",\"PRODUCT_PRICE\":10000}")).isTrue();
    }
}
