package com.classification.domain_system.service;

import com.classification.domain_system.dto.SystemInstallRequest;
import com.classification.domain_system.dto.SystemInstallStatusResponse;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.entity.SystemConfig;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.UserRole;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.SystemConfigRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.SystemConfigRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemInstallService {

    private final SystemConfigRepository configRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleInitializer roleInitializer;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminService keycloakAdminService;

    @Transactional(readOnly = true)
    public SystemInstallStatusResponse getInstallStatus() {
        boolean isInstalled = configRepository.findById("IS_INSTALLED")
                .map(c -> "true".equalsIgnoreCase(c.getConfigValue()))
                .orElse(false);

        boolean hasAdminAccount = userRepository.count() > 0;

        return new SystemInstallStatusResponse(isInstalled, hasAdminAccount);
    }

    @Transactional
    public User installSystem(SystemInstallRequest request) {
        if (configRepository.findById("IS_INSTALLED").map(c -> "true".equalsIgnoreCase(c.getConfigValue())).orElse(false)) {
            throw new IllegalStateException("The system is already installed.");
        }

        if (userRepository.findByUsername(request.getAdminUsername()).isPresent()) {
            throw new IllegalArgumentException("The administrator ID is already in use.");
        }

        log.info("Starting System Setup for Initial Admin: {}", request.getAdminUsername());

        // 1. Create or Find Organization
        Organization organization = new Organization();
        String orgKo = (request.getOrganizationNameKo() != null && !request.getOrganizationNameKo().trim().isEmpty())
                ? request.getOrganizationNameKo().trim()
                : ((request.getOrganizationName() != null && !request.getOrganizationName().trim().isEmpty()) ? request.getOrganizationName().trim() : "본사");
        String orgEn = (request.getOrganizationNameEn() != null && !request.getOrganizationNameEn().trim().isEmpty())
                ? request.getOrganizationNameEn().trim()
                : orgKo;

        organization.setName(orgKo);
        organization.setDisplayName("{\"ko\":\"" + orgKo + "\",\"en\":\"" + orgEn + "\"}");
        organization.setDescription("{\"ko\":\"최초 마스터 대표 조직\",\"en\":\"System Primary Organization\"}");
        organization.setIcon("corporate_fare");
        organization.setIsActive(true);
        Organization savedOrg = organizationRepository.save(organization);

        // 2. Seed Default Roles for Org
        roleInitializer.createDefaultRolesForOrg(savedOrg.getId());
        Role adminRole = roleRepository.findByOrganizationIdAndName(savedOrg.getId(), "ROLE_ADMIN")
                .orElse(null);

        // 3. Create Super Admin User
        User adminUser = new User();
        String adminUsername = request.getAdminUsername().trim();
        adminUser.setUsername(adminUsername);
        adminUser.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        adminUser.setEmail(request.getAdminEmail());
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setOrganizationId(savedOrg.getId());
        adminUser.setIsActive(true);
        if (request.getTimezone() != null) {
            adminUser.setTimezone(request.getTimezone());
        }

        User savedUser = userRepository.save(adminUser);

        // 4. Assign Admin Role in UserRole Table
        if (adminRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(savedUser.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleRepository.save(userRole);
        }

        // 5. Save System Installation Config Flag
        configRepository.save(new SystemConfig("IS_INSTALLED", "true"));
        log.info("System Installation successfully completed. Admin User: {}", savedUser.getUsername());
        
        // 6. Sync Admin User to Keycloak
        try {
            String keycloakEmail = (request.getAdminEmail() != null && !request.getAdminEmail().trim().isEmpty()) 
                ? request.getAdminEmail().trim() 
                : adminUsername + "@example.com";
            keycloakAdminService.createUser(adminUsername, request.getAdminPassword(), keycloakEmail, "System Admin");
        } catch (Exception e) {
            log.error("Failed to sync initial admin user to Keycloak: {}", adminUsername, e);
            throw new RuntimeException("Failed to sync admin user to authentication server.", e);
        }

        return savedUser;
    }
}
