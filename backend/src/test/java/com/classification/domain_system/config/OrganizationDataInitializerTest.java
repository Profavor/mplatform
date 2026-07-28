package com.classification.domain_system.config;

import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.service.RoleInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrganizationDataInitializerTest {

    @Test
    @DisplayName("조직 데이터가 비어있을 때 기본 조직(본사) 및 역할 자동 생성 검증")
    void testInitializeDefaultOrganizationWhenEmpty() {
        OrganizationRepository orgRepository = Mockito.mock(OrganizationRepository.class);
        RoleInitializer roleInitializer = Mockito.mock(RoleInitializer.class);

        when(orgRepository.findAll()).thenReturn(Collections.emptyList());
        when(orgRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization org = invocation.getArgument(0);
            org.setId(java.util.UUID.randomUUID());
            return org;
        });

        OrganizationDataInitializer initializer = new OrganizationDataInitializer(orgRepository, roleInitializer);
        initializer.run(mock(ApplicationArguments.class));

        verify(orgRepository, times(1)).save(any(Organization.class));
        verify(roleInitializer, times(1)).createDefaultRolesForOrg(any());
    }

    @Test
    @DisplayName("이미 조직 데이터가 존재하는 경우 중복 생성하지 않음을 검증")
    void testDoNotCreateWhenOrganizationExists() {
        OrganizationRepository orgRepository = Mockito.mock(OrganizationRepository.class);
        RoleInitializer roleInitializer = Mockito.mock(RoleInitializer.class);

        Organization existingOrg = new Organization();
        existingOrg.setId(java.util.UUID.randomUUID());
        existingOrg.setName("Existing Org");

        when(orgRepository.findAll()).thenReturn(List.of(existingOrg));

        OrganizationDataInitializer initializer = new OrganizationDataInitializer(orgRepository, roleInitializer);
        initializer.run(mock(ApplicationArguments.class));

        verify(orgRepository, never()).save(any(Organization.class));
    }
}
