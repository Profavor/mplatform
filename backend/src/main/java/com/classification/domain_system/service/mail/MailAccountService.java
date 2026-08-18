package com.classification.domain_system.service.mail;

import com.classification.domain_system.dto.MailAccountResponse;
import com.classification.domain_system.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages email accounts in docker-mailserver by manipulating
 * the postfix-accounts.cf configuration file.
 */
@Service
public class MailAccountService {

    private static final Logger log = LoggerFactory.getLogger(MailAccountService.class);

    @Value("${mail.account.config-path:./docker-data/dms/config}")
    private String configPath;

    @Value("${mail.domain:mplatform.com}")
    private String mailDomain;

    @Value("${mail.account.auto-create:true}")
    private boolean autoCreate;

    /**
     * Create a new email account in docker-mailserver.
     */
    public void createAccount(String email, String password) throws IOException {
        Path accountsFile = getAccountsFilePath();
        ensureFileExists(accountsFile);

        // Check if account already exists
        List<String> lines = Files.readAllLines(accountsFile);
        boolean exists = lines.stream().anyMatch(line -> line.startsWith(email + "|"));
        if (exists) {
            log.warn("Mail account already exists: {}", email);
            return;
        }

        // Hash password using SHA-512 (compatible with docker-mailserver)
        String hashedPassword = hashPassword(password);
        lines.add(email + "|" + hashedPassword);
        Files.write(accountsFile, lines);
        log.info("Mail account created: {}", email);
    }

    /**
     * Delete an email account.
     */
    public void deleteAccount(String email) throws IOException {
        Path accountsFile = getAccountsFilePath();
        if (!Files.exists(accountsFile)) return;

        List<String> lines = Files.readAllLines(accountsFile);
        List<String> updatedLines = lines.stream()
                .filter(line -> !line.startsWith(email + "|"))
                .collect(Collectors.toList());
        Files.write(accountsFile, updatedLines);
        log.info("Mail account deleted: {}", email);
    }

    /**
     * Update password for an email account.
     */
    public void updatePassword(String email, String newPassword) throws IOException {
        Path accountsFile = getAccountsFilePath();
        if (!Files.exists(accountsFile)) return;

        String hashedPassword = hashPassword(newPassword);
        List<String> lines = Files.readAllLines(accountsFile);
        List<String> updatedLines = lines.stream()
                .map(line -> line.startsWith(email + "|") ? email + "|" + hashedPassword : line)
                .collect(Collectors.toList());
        Files.write(accountsFile, updatedLines);
        log.info("Mail account password updated: {}", email);
    }

    /**
     * List all email accounts.
     */
    public List<MailAccountResponse> listAccounts() throws IOException {
        Path accountsFile = getAccountsFilePath();
        if (!Files.exists(accountsFile)) return Collections.emptyList();

        return Files.readAllLines(accountsFile).stream()
                .filter(line -> line.contains("|"))
                .map(line -> {
                    String email = line.substring(0, line.indexOf('|'));
                    return MailAccountResponse.builder()
                            .email(email)
                            .isActive(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Sync platform user with mail account.
     * Creates mail account if auto-create is enabled and account doesn't exist.
     */
    public void syncUserAccount(User user) {
        if (!autoCreate) return;
        try {
            String email = user.getUsername() + "@" + mailDomain;
            createAccount(email, generateTempPassword());
        } catch (IOException e) {
            log.error("Failed to sync mail account for user: {}", user.getUsername(), e);
        }
    }

    private Path getAccountsFilePath() {
        return Paths.get(configPath, "postfix-accounts.cf");
    }

    private void ensureFileExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        }
    }

    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String saltHex = bytesToHex(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());
            String hashHex = bytesToHex(hash);

            // Format compatible with docker-mailserver
            return "{SHA512-CRYPT}" + hashHex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }
}
