package com.classification.domain_system.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class KeycloakAdminService {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-client-id}")
    private String clientId;

    @Value("${keycloak.admin-client-secret}")
    private String clientSecret;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            log.warn("KEYCLOAK_ADMIN_CLIENT_SECRET is missing! Keycloak Admin API integration might fail.");
        }
        
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    /**
     * Create a new user in Keycloak.
     */
    public void createUser(String username, String password, String email, String fullName) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setEnabled(true);
            user.setEmailVerified(true);
            
            if (fullName != null) {
                String[] parts = fullName.split(" ", 2);
                if (parts.length > 0) user.setFirstName(parts[0]);
                if (parts.length > 1) user.setLastName(parts[1]);
            }

            Response response = getUsersResource().create(user);
            
            if (response.getStatus() == 201) {
                String userId = org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);
                log.info("Created Keycloak user with ID: {}", userId);
                
                // Set password
                resetPassword(username, password);
            } else if (response.getStatus() == 409) {
                log.warn("User {} already exists in Keycloak. Syncing password.", username);
                resetPassword(username, password);
            } else {
                log.error("Failed to create Keycloak user. Status: {}, Info: {}", response.getStatus(), response.getStatusInfo());
            }
        } catch (Exception e) {
            log.error("Error creating Keycloak user: {}", username, e);
        }
    }

    /**
     * Reset password for an existing user.
     */
    public void resetPassword(String username, String newPassword) {
        try {
            List<UserRepresentation> search = getUsersResource().search(username, true);
            if (search.isEmpty()) {
                log.warn("User {} not found in Keycloak", username);
                return;
            }
            
            String userId = search.get(0).getId();
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);

            getUsersResource().get(userId).resetPassword(credential);
            log.info("Reset password for Keycloak user: {}", username);
        } catch (Exception e) {
            log.error("Error resetting Keycloak password for user: {}", username, e);
        }
    }

    /**
     * Delete a user from Keycloak.
     */
    public void deleteUser(String username) {
        try {
            List<UserRepresentation> search = getUsersResource().search(username, true);
            if (search.isEmpty()) {
                log.warn("User {} not found in Keycloak", username);
                return;
            }
            
            String userId = search.get(0).getId();
            Response response = getUsersResource().delete(userId);
            if (response.getStatus() == 204) {
                log.info("Deleted Keycloak user: {}", username);
            } else {
                log.error("Failed to delete Keycloak user: {}", username);
            }
        } catch (Exception e) {
            log.error("Error deleting Keycloak user: {}", username, e);
        }
    }

    /**
     * Update user details in Keycloak.
     */
    public void updateUser(String username, String email, String fullName, boolean enabled) {
        try {
            List<UserRepresentation> search = getUsersResource().search(username, true);
            if (search.isEmpty()) {
                log.warn("User {} not found in Keycloak", username);
                return;
            }
            
            UserRepresentation user = search.get(0);
            user.setEmail(email);
            user.setEnabled(enabled);
            if (fullName != null) {
                String[] parts = fullName.split(" ", 2);
                if (parts.length > 0) user.setFirstName(parts[0]);
                if (parts.length > 1) user.setLastName(parts[1]);
            }

            getUsersResource().get(user.getId()).update(user);
            log.info("Updated Keycloak user: {}", username);
        } catch (Exception e) {
            log.error("Error updating Keycloak user: {}", username, e);
        }
    }
}
