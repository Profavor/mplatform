package com.classification.domain_system.service;

import org.junit.jupiter.api.BeforeEach;
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
}
