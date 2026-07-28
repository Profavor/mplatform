package com.classification.domain_system.service;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.repository.PermissionGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PermissionMasterInitializerTest {

    @Test
    @DisplayName("PermissionMasterInitializer 실행 시 11개 전체 권한 그룹 및 항목 생성/확충 검증")
    void testInitializeAllPermissionGroups() throws Exception {
        PermissionGroupRepository groupRepository = Mockito.mock(PermissionGroupRepository.class);

        when(groupRepository.findById(anyString())).thenReturn(Optional.empty());
        when(groupRepository.save(any(PermissionGroup.class))).thenAnswer(i -> i.getArgument(0));

        PermissionMasterInitializer initializer = new PermissionMasterInitializer(groupRepository);
        initializer.run();

        // 11개 권한 그룹(admin, domain, node, field, record, dq, org, user, role, workflow, log, match, integration 등)이 저장되는지 검증
        verify(groupRepository, atLeast(11)).save(any(PermissionGroup.class));
    }
}
