package com.classification.domain_system.service;

import com.classification.domain_system.dto.SystemInstallRequest;
import com.classification.domain_system.dto.SystemInstallStatusResponse;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.SystemConfig;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.SystemConfigRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemInstallServiceTest {

    private SystemConfigRepository configRepository;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;
    private com.classification.domain_system.repository.RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    private RoleInitializer roleInitializer;
    private PasswordEncoder passwordEncoder;
    private SystemInstallService installService;

    @BeforeEach
    void setUp() {
        configRepository = Mockito.mock(SystemConfigRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        organizationRepository = Mockito.mock(OrganizationRepository.class);
        roleRepository = Mockito.mock(com.classification.domain_system.repository.RoleRepository.class);
        userRoleRepository = Mockito.mock(UserRoleRepository.class);
        roleInitializer = Mockito.mock(RoleInitializer.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        KeycloakAdminService keycloakAdminService = Mockito.mock(KeycloakAdminService.class);

        installService = new SystemInstallService(
                configRepository,
                userRepository,
                organizationRepository,
                roleRepository,
                userRoleRepository,
                roleInitializer,
                passwordEncoder,
                keycloakAdminService
        );
    }

    @Test
    @DisplayName("미설치 상태에서 최고관리자 계정 및 조직 등록 성공 검증")
    void testInstallSystemSuccess() {
        SystemInstallRequest request = new SystemInstallRequest();
        request.setAdminUsername("superadmin");
        request.setAdminPassword("Password123!");
        request.setOrganizationNameKo("본사");
        request.setOrganizationNameEn("Headquarter");

        when(configRepository.findById("IS_INSTALLED")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pwd");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> {
            Organization org = i.getArgument(0);
            org.setId(UUID.randomUUID());
            return org;
        });
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User installedUser = installService.installSystem(request);

        assertNotNull(installedUser);
        assertEquals("superadmin", installedUser.getUsername());
        assertEquals("ROLE_ADMIN", installedUser.getRole());
        assertEquals("superadmin@example.com", installedUser.getEmail());

        verify(configRepository, times(1)).save(any(SystemConfig.class));
        verify(roleInitializer, times(1)).createDefaultRolesForOrg(any(UUID.class));
    }

    @Test
    @DisplayName("조직 이메일 도메인 입력 시 조직에 저장되고 관리자 이메일이 자동 조합된다")
    void testInstallSystemWithEmailDomain() {
        SystemInstallRequest request = new SystemInstallRequest();
        request.setAdminUsername("admin01");
        request.setAdminPassword("Password123!");
        request.setOrganizationNameKo("본사");
        request.setEmailDomain("@profavor.com");

        when(configRepository.findById("IS_INSTALLED")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("admin01")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pwd");
        
        org.mockito.ArgumentCaptor<Organization> orgCaptor = org.mockito.ArgumentCaptor.forClass(Organization.class);
        when(organizationRepository.save(orgCaptor.capture())).thenAnswer(i -> {
            Organization o = i.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User installedUser = installService.installSystem(request);

        assertNotNull(installedUser);
        assertEquals("admin01@profavor.com", installedUser.getEmail());
        assertEquals("profavor.com", orgCaptor.getValue().getEmailDomain());
    }

    @Test
    @DisplayName("이미 설치가 완료된 시스템에서 중복 설치 시 예외 발생 검증")
    void testInstallSystemAlreadyInstalledThrowsException() {
        SystemInstallRequest request = new SystemInstallRequest();
        request.setAdminUsername("superadmin");
        request.setAdminPassword("Password123!");

        when(configRepository.findById("IS_INSTALLED")).thenReturn(Optional.of(new SystemConfig("IS_INSTALLED", "true")));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> installService.installSystem(request));
        assertTrue(ex.getMessage().contains("The system is already installed."));
    }

    @Test
    @DisplayName("설치 상태 조회 시 설치 여부 및 관리자 계정 유무가 정확히 반환되는지 검증")
    void testGetInstallStatus() {
        when(configRepository.findById("IS_INSTALLED")).thenReturn(Optional.of(new SystemConfig("IS_INSTALLED", "true")));
        when(userRepository.count()).thenReturn(1L);

        SystemInstallStatusResponse status = installService.getInstallStatus();

        assertTrue(status.isInstalled());
        assertTrue(status.isHasAdminAccount());
    }
}
