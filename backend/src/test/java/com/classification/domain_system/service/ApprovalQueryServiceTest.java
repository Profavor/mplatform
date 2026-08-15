package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalQueryServiceTest {

    @Mock private ApprovalRequestRepository approvalRepository;
    @Mock private ApprovalStepRepository stepRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private RecordRepository recordRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private DataMaskingService dataMaskingService;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private ApprovalDelegationService delegationService;

    @InjectMocks
    private ApprovalQueryService approvalQueryService;

    @Test
    @DisplayName("getMyTodos: 역할 컬렉션으로 조회 시 Repository의 findMyPendingStepsForRoles를 호출한다 (Red)")
    void getMyTodos_WithRolesCollection() {
        String assigneeId = UUID.randomUUID().toString();
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        Pageable pageable = PageRequest.of(0, 10);
        Page<ApprovalStep> mockPage = new PageImpl<>(List.of(new ApprovalStep()));
        
        when(stepRepository.findMyPendingStepsForRoles(eq(assigneeId), eq(roles), eq("PENDING"), eq(pageable)))
                .thenReturn(mockPage);

        Page<ApprovalStep> result = approvalQueryService.getMyTodos(assigneeId, roles, pageable);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(stepRepository).findMyPendingStepsForRoles(assigneeId, roles, "PENDING", pageable);
    }

    @Test
    @DisplayName("getAllRequests: 허용되지 않은 필터 키가 포함된 경우 BusinessException(INVALID_INPUT) 예외가 발생한다")
    @SuppressWarnings("unchecked")
    void getAllRequests_WithInvalidFilterKey_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        String invalidFilterModel = "{\"unauthorized_column\": {\"filterType\": \"text\", \"filter\": \"abc\"}}";

        when(approvalRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    org.springframework.data.jpa.domain.Specification<?> spec = invocation.getArgument(0);
                    if (spec != null) {
                        spec.toPredicate(null, null, null);
                    }
                    return Page.empty();
                });

        assertThatThrownBy(() -> approvalQueryService.getAllRequests(null, null, invalidFilterModel, pageable))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("getRequestById: ID로 결재 요청 단건 조회 시 마스킹 처리를 진행하고 반환한다")
    void getRequestById_Success() {
        UUID id = UUID.randomUUID();
        ApprovalRequest mockRequest = new ApprovalRequest();
        mockRequest.setId(id);
        mockRequest.setChanges("{\"secret\":\"value\"}");
        
        when(approvalRepository.findById(id)).thenReturn(Optional.of(mockRequest));
        when(dataMaskingService.maskChangesJson(any(), any(), eq(false))).thenReturn("{\"secret\":\"*****\"}");

        ApprovalRequest result = approvalQueryService.getRequestById(id);
        assertThat(result).isNotNull();
        assertThat(result.getChanges()).isEqualTo("{\"secret\":\"*****\"}");
    }
}
