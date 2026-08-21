package com.classification.domain_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class VaultTransitService {

    private final RestTemplate restTemplate;
    private final String vaultUri;
    private final String authMethod;
    private final String token;
    private final String k8sRole;
    private final String k8sJwtPath;
    private final String keyName;

    private String cachedClientToken;
    private Instant tokenExpiration = Instant.MIN;

    @Autowired
    public VaultTransitService(
            @Value("${security.encryption.vault.uri:http://localhost:8200}") String vaultUri,
            @Value("${security.encryption.vault.auth-method:TOKEN}") String authMethod,
            @Value("${security.encryption.vault.token:#{null}}") String token,
            @Value("${security.encryption.vault.k8s-role:mdm-role}") String k8sRole,
            @Value("${security.encryption.vault.k8s-jwt-path:/var/run/secrets/kubernetes.io/serviceaccount/token}") String k8sJwtPath,
            @Value("${security.encryption.vault.key-name:mdm-field-key}") String keyName) {
        this(new RestTemplate(), vaultUri, authMethod, token, k8sRole, k8sJwtPath, keyName);
    }

    public VaultTransitService(
            RestTemplate restTemplate,
            String vaultUri,
            String authMethod,
            String token,
            String k8sRole,
            String k8sJwtPath,
            String keyName) {
        this.restTemplate = restTemplate;
        this.vaultUri = vaultUri != null ? vaultUri.replaceAll("/+$", "") : "http://localhost:8200";
        this.authMethod = authMethod != null ? authMethod.toUpperCase() : "TOKEN";
        this.token = token;
        this.k8sRole = k8sRole != null ? k8sRole : "mdm-role";
        this.k8sJwtPath = k8sJwtPath != null ? k8sJwtPath : "/var/run/secrets/kubernetes.io/serviceaccount/token";
        this.keyName = keyName != null ? keyName : "mdm-field-key";
    }

    public synchronized String getClientToken() {
        if ("TOKEN".equals(authMethod) && token != null && !token.isBlank()) {
            return token;
        }

        if (cachedClientToken != null && Instant.now().isBefore(tokenExpiration)) {
            return cachedClientToken;
        }

        if ("KUBERNETES".equals(authMethod)) {
            try {
                Path path = Paths.get(k8sJwtPath);
                if (!Files.exists(path)) {
                    log.warn("Kubernetes ServiceAccount token file not found at {}. Fallback to configured token if any.", k8sJwtPath);
                    return token;
                }
                String jwt = Files.readString(path).trim();
                String loginUrl = vaultUri + "/v1/auth/kubernetes/login";
                Map<String, String> body = Map.of("role", k8sRole, "jwt", jwt);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.exchange(loginUrl, HttpMethod.POST, request, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> auth = (Map<String, Object>) response.getBody().get("auth");
                    if (auth != null && auth.get("client_token") != null) {
                        cachedClientToken = (String) auth.get("client_token");
                        Number leaseDuration = (Number) auth.getOrDefault("lease_duration", 3600);
                        // Refresh 60 seconds before expiration
                        long validSeconds = Math.max(60, leaseDuration.longValue() - 60);
                        tokenExpiration = Instant.now().plusSeconds(validSeconds);
                        log.info("Successfully authenticated with Vault using Kubernetes Auth. Token valid for {}s", validSeconds);
                        return cachedClientToken;
                    }
                }
            } catch (java.io.IOException e) {
                log.error("Failed to read Kubernetes ServiceAccount token from {}: {}", k8sJwtPath, e.getMessage());
            } catch (org.springframework.web.client.RestClientException e) {
                log.error("Failed to authenticate with Vault via Kubernetes Auth: {}", e.getMessage());
            }
        }

        return token != null && !token.isBlank() ? token : "root";
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            String base64Plain = Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
            String url = vaultUri + "/v1/transit/encrypt/" + keyName;

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("plaintext", base64Plain), headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.get("ciphertext") != null) {
                    return (String) data.get("ciphertext");
                }
            }
            throw new com.classification.domain_system.exception.EncryptionException("Empty response from Vault Transit Encrypt");
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Vault Transit encryption failed for key {}: {}", keyName, e.getMessage());
            throw new com.classification.domain_system.exception.EncryptionException("Vault encryption error: " + e.getMessage(), e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        if (!isVaultEncrypted(cipherText)) {
            return cipherText;
        }
        try {
            String url = vaultUri + "/v1/transit/decrypt/" + keyName;

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("ciphertext", cipherText), headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.get("plaintext") != null) {
                    String base64Plain = (String) data.get("plaintext");
                    byte[] decoded = Base64.getDecoder().decode(base64Plain);
                    return new String(decoded, StandardCharsets.UTF_8);
                }
            }
            throw new com.classification.domain_system.exception.DecryptionException("Empty response from Vault Transit Decrypt");
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Vault Transit decryption failed for cipher: {}", e.getMessage());
            throw new com.classification.domain_system.exception.DecryptionException("Vault decryption error: " + e.getMessage(), e);
        }
    }

    public String generateHmac(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            String base64Plain = Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
            String url = vaultUri + "/v1/transit/hmac/" + keyName;

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("input", base64Plain), headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.get("hmac") != null) {
                    return (String) data.get("hmac");
                }
            }
            throw new com.classification.domain_system.exception.EncryptionException("Empty response from Vault Transit HMAC");
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Vault Transit HMAC generation failed: {}", e.getMessage());
            throw new com.classification.domain_system.exception.EncryptionException("Vault HMAC error: " + e.getMessage(), e);
        }
    }

    public String rewrap(String cipherText) {
        if (cipherText == null || cipherText.isEmpty() || !isVaultEncrypted(cipherText)) {
            return cipherText;
        }
        try {
            String url = vaultUri + "/v1/transit/rewrap/" + keyName;

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("ciphertext", cipherText), headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.get("ciphertext") != null) {
                    return (String) data.get("ciphertext");
                }
            }
            throw new com.classification.domain_system.exception.EncryptionException("Empty response from Vault Transit Rewrap");
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Vault Transit Rewrap failed: {}", e.getMessage());
            throw new com.classification.domain_system.exception.EncryptionException("Vault rewrap error: " + e.getMessage(), e);
        }
    }

    public boolean isVaultEncrypted(String text) {
        return text != null && text.startsWith("vault:v");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        String clientToken = getClientToken();
        if (clientToken != null && !clientToken.isBlank()) {
            headers.set("X-Vault-Token", clientToken);
        }
        return headers;
    }
}
