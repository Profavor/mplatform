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

    @Test
    void testChangePassword_ByUserId() {
        String userId = "4d06f8c8-54a3-43fe-89da-d794cbff7f5a";
        User user = new User();
        user.setId(userId);
        user.setUsername("test2");
        user.setPassword("encodedOld");
        user.setMustChangePassword(true);

        when(userRepository.findByUsername(userId)).thenReturn(java.util.Optional.empty());
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("OldPass!", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1234!")).thenReturn("encodedNew");

        userService.changePassword(userId, "OldPass!", "NewPass1234!");

        assertThat(user.getPassword()).isEqualTo("encodedNew");
        assertThat(user.getMustChangePassword()).isFalse();
        verify(userRepository).save(user);
        verify(keycloakAdminService).resetPassword("test2", "NewPass1234!");
    }

    @Test
    void testUpdateAdminUserInfo_EmailChange_SyncsToKeycloak() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("old@example.com");
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        com.classification.domain_system.dto.AdminUserUpdateDto dto = new com.classification.domain_system.dto.AdminUserUpdateDto();
        dto.setEmail("new@example.com");
        dto.setRole("ROLE_ADMIN");

        User updated = userService.updateAdminUserInfo(userId, dto);

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getRole()).isEqualTo("ROLE_ADMIN");
        verify(userRepository).save(user);
        verify(keycloakAdminService).updateUser(eq("testuser"), eq("new@example.com"), eq("testuser"), eq(true));
    }

    @Test
    void testUpdateAdminUserInfo_DuplicateEmail_ThrowsException() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("current@example.com");

        User existingOther = new User();
        existingOther.setId("other-user-999");
        existingOther.setUsername("otheruser");
        existingOther.setEmail("duplicate@example.com");

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(java.util.Optional.of(existingOther));

        com.classification.domain_system.dto.AdminUserUpdateDto dto = new com.classification.domain_system.dto.AdminUserUpdateDto();
        dto.setEmail("duplicate@example.com");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.updateAdminUserInfo(userId, dto);
        });
    }

    @Test
    void testUpdateSelfUserInfo_EmailChange_SyncsToKeycloak() {
        String username = "selfuser";
        User user = new User();
        user.setId("self-123");
        user.setUsername(username);
        user.setEmail("old_self@example.com");
        user.setIsActive(true);

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
        when(userRepository.findByEmail("new_self@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        com.classification.domain_system.dto.SelfUserUpdateDto dto = new com.classification.domain_system.dto.SelfUserUpdateDto();
        dto.setEmail("new_self@example.com");
        dto.setTimezone("Asia/Tokyo");

        User updated = userService.updateSelfUserInfo(username, dto);

        assertThat(updated.getEmail()).isEqualTo("new_self@example.com");
        assertThat(updated.getTimezone()).isEqualTo("Asia/Tokyo");
        verify(userRepository).save(user);
        verify(keycloakAdminService).updateUser(eq(username), eq("new_self@example.com"), eq(username), eq(true));
    }
}
