package com.classification.domain_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultTransitServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private VaultTransitService vaultTransitService;

    @BeforeEach
    void setUp() {
        vaultTransitService = new VaultTransitService(
                restTemplate,
                "http://localhost:8200",
                "TOKEN",
                "root",
                "mdm-role",
                "/var/run/secrets/kubernetes.io/serviceaccount/token",
                "mdm-field-key"
        );
    }

    @Test
    @DisplayName("encrypt - 평문을 Vault Transit 엔진으로 암호화하여 vault:v1:... 암호문을 반환한다")
    void testEncrypt_Success() {
        String plainText = "860104-1234567";
        String expectedCipher = "vault:v1:SGVsbG9Xb3JsZA==";

        Map<String, Object> responseBody = Map.of(
                "data", Map.of("ciphertext", expectedCipher)
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8200/v1/transit/encrypt/mdm-field-key"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        String result = vaultTransitService.encrypt(plainText);

        assertThat(result).isEqualTo(expectedCipher);
    }

    @Test
    @DisplayName("decrypt - vault:v1:... 암호문을 Vault Transit 엔진으로 복호화하여 원본 평문을 반환한다")
    void testDecrypt_Success() {
        String cipherText = "vault:v1:SGVsbG9Xb3JsZA==";
        String originalPlain = "860104-1234567";
        String base64Plain = Base64.getEncoder().encodeToString(originalPlain.getBytes());

        Map<String, Object> responseBody = Map.of(
                "data", Map.of("plaintext", base64Plain)
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8200/v1/transit/decrypt/mdm-field-key"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        String result = vaultTransitService.decrypt(cipherText);

        assertThat(result).isEqualTo(originalPlain);
    }

    @Test
    @DisplayName("generateHmac - 평문에 대한 Blind Index용 HMAC 해시를 생성한다")
    void testGenerateHmac_Success() {
        String plainText = "860104-1234567";
        String expectedHmac = "vault:v1:hmac-hash-value-12345";

        Map<String, Object> responseBody = Map.of(
                "data", Map.of("hmac", expectedHmac)
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8200/v1/transit/hmac/mdm-field-key"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        String result = vaultTransitService.generateHmac(plainText);

        assertThat(result).isEqualTo(expectedHmac);
    }

    @Test
    @DisplayName("rewrap - 과거 키 버전(v1)의 암호문을 최신 키 버전(v2)으로 재암호화한다")
    void testRewrap_Success() {
        String oldCipher = "vault:v1:old-cipher-data";
        String newCipher = "vault:v2:new-cipher-data";

        Map<String, Object> responseBody = Map.of(
                "data", Map.of("ciphertext", newCipher)
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8200/v1/transit/rewrap/mdm-field-key"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        String result = vaultTransitService.rewrap(oldCipher);

        assertThat(result).isEqualTo(newCipher);
    }
}
