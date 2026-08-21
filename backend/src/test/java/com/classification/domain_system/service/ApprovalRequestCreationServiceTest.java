package com.classification.domain_system.service;

import com.classification.domain_system.dto.MemoApprovalRequest;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalRequestCreationServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRepository;
    @Mock
    private ApprovalStepRepository stepRepository;
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private ClassificationNodeRepository nodeRepository;
    @Mock
    private WorkflowResolver workflowResolver;
    @Mock
    private ApprovalNotificationFacade notificationFacade;
    @Mock
    private DataQualityService dqService;
    @Mock
    private MatchingService matchingService;
    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock
    private CalculatedFieldEvaluator calculatedFieldEvaluator;
    @Mock
    private ApprovalFieldPermissionService permissionService;

    @InjectMocks
    private ApprovalRequestCreationService creationService;

    private UUID nodeId;
    private ClassificationNode mockNode;
    private Domain mockDomain;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        mockDomain = new Domain();
        mockDomain.setId(UUID.randomUUID());
        mockDomain.setIdentifierFieldId(UUID.randomUUID());
        mockDomain.setDisplayNameFieldId(UUID.randomUUID());

        mockNode = new ClassificationNode();
        mockNode.setId(nodeId);
        mockNode.setDomain(mockDomain);
    }

    @Test
    @DisplayName("requestRecordCreation - 도메인 매핑 필드 누락 시 BusinessException 발생")
    void requestRecordCreation_MissingDomainMapping_ThrowsException() {
        mockDomain.setIdentifierFieldId(null);
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(mockNode));

        RecordRequest request = new RecordRequest();
        request.setData("{\"key\":\"val\"}");

        assertThrows(BusinessException.class, () ->
            creationService.requestRecordCreation(nodeId, request)
        );
    }

    @Test
    @DisplayName("requestRecordCreation - 성공적으로 ApprovalRequest 생성 및 알림 발송")
    void requestRecordCreation_Success() {
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(mockNode));
        when(workflowResolver.resolveWorkflow(eq(nodeId), eq("CREATE"))).thenReturn(new WorkflowConfig());
        DataQualityService.DQResult dq = new DataQualityService.DQResult();
        dq.isValid = true;
        dq.errors = List.of();
        when(dqService.validateData(eq(nodeId), any(), any(), any())).thenReturn(dq);

        MatchingService.DuplicateResult dup = new MatchingService.DuplicateResult();
        dup.hasDuplicates = false;
        when(matchingService.checkDuplicates(eq(nodeId), any())).thenReturn(dup);
        when(calculatedFieldEvaluator.recomputeCalculatedFields(eq(nodeId), any())).thenAnswer(i -> i.getArgument(1));
        when(recordRepository.save(any())).thenAnswer(i -> {
            Record r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(approvalRepository.save(any())).thenAnswer(i -> {
            ApprovalRequest a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        RecordRequest request = new RecordRequest();
        request.setData("{\"field_id\":\"123\", \"field_name\":\"홍길동\"}");
        request.setRequesterId("user1");

        ApprovalRequest result = creationService.requestRecordCreation(nodeId, request);

        assertNotNull(result);
        assertEquals(ApprovalStatus.PENDING.name(), result.getStatus());
        assertEquals("RECORD", result.getTargetType());
        verify(approvalRepository).save(any());
        verify(notificationFacade).publishApprovalRequestCreated(any());
    }

    @Test
    @DisplayName("requestMemoApproval - 메모 결재 상신 성공")
    void requestMemoApproval_Success() {
        MemoApprovalRequest request = new MemoApprovalRequest();
        request.setTitle("기안 문서 제목");
        request.setContent("기안 내용 상세");
        MemoApprovalRequest.MemoStepItem step1 = new MemoApprovalRequest.MemoStepItem();
        step1.setAssigneeId("approver1");
        step1.setStepOrder(1);
        request.setSteps(List.of(step1));

        when(approvalRepository.save(any())).thenAnswer(i -> {
            ApprovalRequest a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        ApprovalRequest result = creationService.requestMemoApproval(request, "user1");

        assertNotNull(result);
        assertEquals("MEMO", result.getTargetType());
        assertEquals(ApprovalStatus.PENDING.name(), result.getStatus());
        verify(approvalRepository).save(any());
        verify(notificationFacade).publishApprovalRequestCreated(any());
    }
}
