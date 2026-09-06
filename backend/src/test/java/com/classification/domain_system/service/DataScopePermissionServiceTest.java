package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataScopePermissionDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.DataScopePermission;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DataScopePermissionRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataScopePermissionServiceTest {

    @Mock private DataScopePermissionRepository scopeRepository;
    @Mock private UserRepository userRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private PermissionAuditService auditService;

    @InjectMocks
    private DataScopePermissionService scopeService;

    private User testUser;
    private Domain testDomain;
    private ClassificationNode testNode;
    private UUID domainId;
    private UUID nodeId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        nodeId = UUID.randomUUID();

        testUser = new User();
        testUser.setId("u-user-01");
        testUser.setUsername("operator1");
        testUser.setRole("ROLE_USER");

        testDomain = new Domain();
        testDomain.setId(domainId);
        testDomain.setName(Map.of("ko", "고객 도메인"));

        testNode = new ClassificationNode();
        testNode.setId(nodeId);
        testNode.setName(Map.of("ko", "개인 고객"));
    }

    @Test
    @DisplayName("grantScope: 도메인/노드 스코프 부여 시 정확히 저장되고 감사 로그가 기록되어야 한다")
    void testGrantScope_SavesScopeAndRecordsAuditLog() {
        when(userRepository.findById("u-user-01")).thenReturn(Optional.of(testUser));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(testDomain));
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(testNode));
        when(scopeRepository.findByUserIdAndDomainIdAndNodeId("u-user-01", domainId, nodeId))
                .thenReturn(Optional.empty());

        when(scopeRepository.save(any(DataScopePermission.class))).thenAnswer(inv -> {
            DataScopePermission p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        DataScopePermissionDto.Request req = DataScopePermissionDto.Request.builder()
                .domainId(domainId)
                .nodeId(nodeId)
                .permissionLevel("WRITE")
                .build();

        DataScopePermissionDto.Response res = scopeService.grantScope("u-user-01", req, "admin");

        assertThat(res).isNotNull();
        assertThat(res.getPermissionLevel()).isEqualTo("WRITE");
        assertThat(res.getScopeCode()).startsWith("SCP-");

        verify(auditService, times(1)).recordLog(
                eq("u-user-01"), eq("operator1"), eq("GRANT_SCOPE"), eq("DATA_SCOPE"),
                anyString(), contains("고객 도메인"), isNull(), eq("Level=WRITE"), eq("admin"), isNull()
        );
    }

    @Test
    @DisplayName("hasScopeAccess: 최고 관리자(ROLE_ADMIN)는 스코프 지정 없이도 모든 도메인/노드 접근 허용")
    void testHasScopeAccess_AdminHasUnrestrictedAccess() {
        User admin = new User();
        admin.setId("u-admin");
        admin.setRole("ROLE_ADMIN");

        boolean canAccess = scopeService.hasScopeAccess(admin, domainId, nodeId, "ADMIN");
        assertThat(canAccess).isTrue();
    }

    @Test
    @DisplayName("hasScopeAccess: 일반 사용자는 부여된 도메인 스코프 레벨(READ/WRITE)에 따라 정확히 인가 판정")
    void testHasScopeAccess_EvaluatesUserScopeLevelCorrectly() {
        DataScopePermission readScope = DataScopePermission.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .domain(testDomain)
                .node(null) // 도메인 전체
                .permissionLevel("READ")
                .build();

        when(scopeRepository.findByUserIdAndDomainId("u-user-01", domainId))
                .thenReturn(List.of(readScope));

        // READ 레벨 요청 -> 허용
        assertThat(scopeService.hasScopeAccess(testUser, domainId, nodeId, "READ")).isTrue();

        // WRITE 레벨 요청 -> 거부 (READ만 보유)
        assertThat(scopeService.hasScopeAccess(testUser, domainId, nodeId, "WRITE")).isFalse();
    }

    @Test
    @DisplayName("revokeScope: 스코프 회수 시 엔티티 삭제 및 감사 로그 기록")
    void testRevokeScope_DeletesScopeAndRecordsAuditLog() {
        UUID scopeId = UUID.randomUUID();
        DataScopePermission scope = DataScopePermission.builder()
                .id(scopeId)
                .user(testUser)
                .domain(testDomain)
                .node(testNode)
                .permissionLevel("WRITE")
                .build();

        when(scopeRepository.findById(scopeId)).thenReturn(Optional.of(scope));

        scopeService.revokeScope(scopeId, "admin");

        verify(scopeRepository, times(1)).delete(scope);
        verify(auditService, times(1)).recordLog(
                eq("u-user-01"), eq("operator1"), eq("REVOKE_SCOPE"), eq("DATA_SCOPE"),
                eq(scopeId.toString()), anyString(), eq("Level=WRITE"), isNull(), eq("admin"), isNull()
        );
    }
}
