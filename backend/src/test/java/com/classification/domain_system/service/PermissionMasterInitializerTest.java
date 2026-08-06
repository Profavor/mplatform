package com.classification.domain_system.service;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.repository.PermissionGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionMasterInitializerTest {

    @Mock
    private PermissionGroupRepository groupRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private PermissionMasterInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new PermissionMasterInitializer(groupRepository, objectMapper);
    }

    @Test
    @DisplayName("PermissionMasterInitializer 실행 시 11개 전체 권한 그룹 및 항목 생성/확충 검증")
    void testInitializeAllPermissionGroups() throws Exception {
        when(groupRepository.findById(anyString())).thenReturn(Optional.empty());
        when(groupRepository.save(any(PermissionGroup.class))).thenAnswer(i -> i.getArgument(0));

        initializer.run();

        // 11개 권한 그룹(admin, domain, node, field, record, dq, org, user, role, workflow, log, match, integration 등)이 저장되는지 검증
        verify(groupRepository, atLeast(11)).save(any(PermissionGroup.class));
    }

    @Test
    @DisplayName("이미 권한 마스터 데이터가 존재할 경우(count > 0) 초기화를 건너뛴다")
    void testSkipInitializationWhenDataExists() throws Exception {
        when(groupRepository.count()).thenReturn(5L);

        initializer.run();

        verify(groupRepository, never()).save(any(PermissionGroup.class));
    }
}
