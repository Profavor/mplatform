package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.service.FieldEncryptionService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FieldEncryptionService fieldEncryptionService;
    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private UserService userService;

    @Test
    void testSearchUsers() {
        // Given
        User u1 = new User();
        u1.setId("1");
        u1.setUsername("admin");
        
        User u2 = new User();
        u2.setId("2");
        u2.setUsername("admin2");

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(Arrays.asList(u1, u2), pageable, 2);

        when(userRepository.findByUsernameContainingIgnoreCaseOrRoleContainingIgnoreCase(eq("admin"), eq("admin"), eq(pageable)))
                .thenReturn(mockPage);

        // When
        Page<User> result = userService.searchUsers("admin", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testCreateAdminUser_WithExplicitEmail() {
        // Given
        String username = "testuser";
        String email = "custom@mycorp.com";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(fieldEncryptionService.encrypt(anyString())).thenReturn("encTemp");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        String tempPass = userService.createAdminUser(username, email, "ROLE_USER", null, null);

        // Then
        assertThat(tempPass).isNotNull();
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("custom@mycorp.com");
        verify(keycloakAdminService).createUser(
                eq("testuser"),
                eq(tempPass),
                eq("custom@mycorp.com"),
                eq("testuser")
        );
    }

    @Test
    void testCreateAdminUser_WithOrgEmailDomain() {
        // Given
        String username = "orguser";
        java.util.UUID orgId = java.util.UUID.randomUUID();
        Organization testOrg = new Organization();
        testOrg.setId(orgId);
        testOrg.setEmailDomain("profavor.com");

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());
        when(organizationRepository.findById(orgId)).thenReturn(java.util.Optional.of(testOrg));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(fieldEncryptionService.encrypt(anyString())).thenReturn("encTemp");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When (email is empty)
        String tempPass = userService.createAdminUser(username, "", "ROLE_USER", orgId, null);

        // Then
        assertThat(tempPass).isNotNull();
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("orguser@profavor.com");
        verify(keycloakAdminService).createUser(
                eq("orguser"),
                eq(tempPass),
                eq("orguser@profavor.com"),
                eq("orguser")
        );
    }

    @Test
    void testCreateAdminUser_FallbackToExampleCom() {
        // Given
        String username = "fallbackuser";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(fieldEncryptionService.encrypt(anyString())).thenReturn("encTemp");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When (no email and no orgId)
        String tempPass = userService.createAdminUser(username, null, "ROLE_USER", null, null);

        // Then
        assertThat(tempPass).isNotNull();
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("fallbackuser@example.com");
        verify(keycloakAdminService).createUser(
                eq("fallbackuser"),
                eq(tempPass),
                eq("fallbackuser@example.com"),
                eq("fallbackuser")
        );
    }
}
