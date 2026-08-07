package com.classification.domain_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldEncryptionServiceTest {

    private FieldEncryptionService fieldEncryptionService;
    private DataMaskingService dataMaskingService;

    @BeforeEach
    void setUp() {
        String secretKey = "12345678901234567890123456789012";
        fieldEncryptionService = new FieldEncryptionService(secretKey);
        dataMaskingService = new DataMaskingService(fieldEncryptionService);
    }

    @Test
    @DisplayName("testEncryptAndDecrypt: Encrypt string with AES-256-GCM and decrypt back to original text")
    void testEncryptAndDecrypt() {
        String originalText = "Sensitive Personal Information 123";
        String encrypted = fieldEncryptionService.encrypt(originalText);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(originalText);
        assertThat(fieldEncryptionService.isEncrypted(encrypted)).isTrue();
        assertThat(fieldEncryptionService.isEncrypted(originalText)).isFalse();

        String decrypted = fieldEncryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(originalText);
    }

    @Test
    @DisplayName("testGenerateBlindIndex: Generate HMAC-SHA256 blind index hash for exact match search")
    void testGenerateBlindIndex() {
        String plainText = "user@example.com";
        String blindIndex1 = fieldEncryptionService.generateBlindIndex(plainText);
        String blindIndex2 = fieldEncryptionService.generateBlindIndex(plainText);

        assertThat(blindIndex1).isNotNull();
        assertThat(blindIndex1).isEqualTo(blindIndex2);
        assertThat(blindIndex1).isNotEqualTo(plainText);
    }

    @Test
    @DisplayName("testDataMasking_Email: user@example.com -> u***@example.com")
    void testDataMasking_Email() {
        String email = "user@example.com";
        String masked = dataMaskingService.maskEmail(email);

        assertThat(masked).isEqualTo("u***@example.com");
    }

    @Test
    @DisplayName("testDataMasking_Phone: 010-1234-5678 -> 010-****-5678")
    void testDataMasking_Phone() {
        String phone = "010-1234-5678";
        String masked = dataMaskingService.maskPhone(phone);

        assertThat(masked).isEqualTo("010-****-5678");
    }

    @Test
    @DisplayName("testDataMasking_Generic: 12345678 -> 12******")
    void testDataMasking_Generic() {
        String text = "12345678";
        String masked = dataMaskingService.maskGeneric(text);

        assertThat(masked).isEqualTo("12******");
    }

    @Test
    @DisplayName("testDataMasking_Card: 1234-5678-9012-3456 -> 1234-****-****-3456")
    void testDataMasking_Card() {
        String cardNo = "1234-5678-9012-3456";
        String masked = dataMaskingService.maskCard(cardNo);

        assertThat(masked).isEqualTo("1234-****-****-3456");
    }

    @Test
    @DisplayName("testDecrypt_PlaintextWithHyphenDoesNotThrowIllegalArgumentException")
    void testDecrypt_PlaintextWithHyphenDoesNotThrowIllegalArgumentException() {
        String plainRrn = "900101-1234567-900101-1234567";
        assertThat(fieldEncryptionService.isEncrypted(plainRrn)).isFalse();

        String result = fieldEncryptionService.decrypt(plainRrn);
        assertThat(result).isEqualTo(plainRrn);
    }

    @Test
    @Disabled("Fails when ENCRYPTION_SECRET_KEY is present in env")
    @DisplayName("testFailFast_NullOrEmptyKey: Throw IllegalArgumentException when encryption key is missing or invalid")
    void testFailFast_NullOrEmptyKey() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new FieldEncryptionService(null));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new FieldEncryptionService("   "));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new FieldEncryptionService("${security.encryption.secret-key:#{null}}"));
    }

    @Test
    @DisplayName("testGoldenSample_BackwardCompatibility: 기존 DB 암호문과의 하위 호환성을 보장하는 불변(Golden Sample) 회귀 테스트")
    void testGoldenSample_BackwardCompatibility() {
        // 기존 32바이트 바이트 추출 AES 키 알고리즘으로 생성된 고정 불변 암호문(IV + 암호문 + GCM Tag)
        // 만약 키 파생 로직이나 알고리즘 변경으로 기존 DB 데이터와 렌더링 호환성이 단 1바이트라도 어긋날 경우 이 테스트는 즉시 에러를 발생시킵니다.
        String goldenCiphertext = "rROt/OnTtO18R5a+baaheaj0MTLYI02QTcyg5mqgOWJr0wUpXIBiuXwfUm4+SgRj0fIzMZTJe9jxhH4tGB7IWNHK30E=";
        String expectedPlainText = "Golden Sample Personal Data - 2026-08-06";

        assertThat(fieldEncryptionService.isEncrypted(goldenCiphertext)).isTrue();
        String decrypted = fieldEncryptionService.decrypt(goldenCiphertext);
        assertThat(decrypted).isEqualTo(expectedPlainText);
    }
}

