package com.classification.domain_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Slf4j
public class FieldEncryptionService {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey aesKey;
    private final SecretKey hmacKey;
    private final String encryptionType;
    private final VaultTransitService vaultTransitService;

    public FieldEncryptionService(String secretKey) {
        this(secretKey, "LOCAL", null);
    }

    @Autowired
    public FieldEncryptionService(
            @Value("${security.encryption.secret-key:#{null}}") String secretKey,
            @Value("${security.encryption.type:LOCAL}") String encryptionType,
            @Autowired(required = false) VaultTransitService vaultTransitService) {
        this.encryptionType = encryptionType != null ? encryptionType.toUpperCase() : "LOCAL";
        this.vaultTransitService = vaultTransitService;

        if (secretKey == null || secretKey.isBlank() || secretKey.startsWith("${")) {
            secretKey = System.getenv("ENCRYPTION_SECRET_KEY");
        }
        if (secretKey == null || secretKey.isBlank() || secretKey.startsWith("${")) {
            secretKey = "default-vault-local-fallback-key-32bytes";
        }
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                byte[] padded = new byte[32];
                System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
                keyBytes = padded;
            } else if (keyBytes.length > 32) {
                byte[] truncated = new byte[32];
                System.arraycopy(keyBytes, 0, truncated, 0, 32);
                keyBytes = truncated;
            }
            this.aesKey = new SecretKeySpec(keyBytes, "AES");

            java.security.MessageDigest sha256 = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hmacKeyBytes = sha256.digest(("HMAC-BLIND-INDEX-KEY:" + secretKey).getBytes(StandardCharsets.UTF_8));
            this.hmacKey = new SecretKeySpec(hmacKeyBytes, HMAC_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption keys", e);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        // 1. Vault Transit KMS Mode
        if ("VAULT".equalsIgnoreCase(encryptionType) && vaultTransitService != null) {
            try {
                return vaultTransitService.encrypt(plainText);
            } catch (Exception e) {
                log.warn("Vault encryption failed, falling back to local AES-GCM: {}", e.getMessage());
            }
        }

        // 2. Local AES-GCM Mode
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

            byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherTextBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherTextBytes, 0, combined, iv.length, cipherTextBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES-GCM Encryption failed", e);
            throw new RuntimeException("Encryption error", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        // 1. If text is encrypted by Vault Transit (vault:v1:...)
        if (vaultTransitService != null && vaultTransitService.isVaultEncrypted(cipherText)) {
            try {
                return vaultTransitService.decrypt(cipherText);
            } catch (Exception e) {
                log.error("Vault decryption failed: {}", e.getMessage());
                return cipherText;
            }
        }

        // 2. Legacy Local AES-GCM decryption
        if (!isLegacyEncrypted(cipherText)) {
            return cipherText;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            if (combined.length < GCM_IV_LENGTH + 16) {
                return cipherText;
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            byte[] cipherTextBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, cipherTextBytes, 0, cipherTextBytes.length);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

            byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.trace("AES-GCM Decryption skipped or failed for string: {}", e.getMessage());
            return cipherText;
        }
    }

    public String generateBlindIndex(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        // 1. Vault Transit HMAC Mode
        if ("VAULT".equalsIgnoreCase(encryptionType) && vaultTransitService != null) {
            try {
                return vaultTransitService.generateHmac(plainText);
            } catch (Exception e) {
                log.warn("Vault HMAC generation failed, falling back to local HMAC-SHA256: {}", e.getMessage());
            }
        }

        // 2. Local HMAC-SHA256 Mode
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKey);
            byte[] hmacBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            log.error("HMAC blind index generation failed", e);
            throw new RuntimeException("Blind index error", e);
        }
    }

    public String rewrap(String cipherText) {
        if (vaultTransitService != null && vaultTransitService.isVaultEncrypted(cipherText)) {
            return vaultTransitService.rewrap(cipherText);
        }
        return cipherText;
    }

    public boolean isEncrypted(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (vaultTransitService != null && vaultTransitService.isVaultEncrypted(text)) {
            return true;
        }
        if (text.startsWith("vault:v")) {
            return true;
        }
        return isLegacyEncrypted(text);
    }

    private boolean isLegacyEncrypted(String text) {
        if (text == null || text.isBlank() || text.length() < 24) {
            return false;
        }
        if (!text.matches("^[A-Za-z0-9+/=]+$")) {
            return false;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(text);
            return combined.length >= GCM_IV_LENGTH + 16;
        } catch (Exception e) {
            return false;
        }
    }
}

