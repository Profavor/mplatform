package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.springframework.context.ApplicationEventPublisher;
import com.classification.domain_system.event.ApprovalRequestCreatedEvent;
import com.classification.domain_system.event.ApprovalStepApprovedEvent;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class ApprovalServiceTest extends BaseServiceTest {

    @Mock private ApprovalRequestRepository approvalRepository;
    @Mock private ApprovalStepRepository stepRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private WorkflowConfigRepository workflowConfigRepository;
    @Mock private DataQualityService dqService;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private MatchingService matchingService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private CalculatedFieldEvaluator calculatedFieldEvaluator;
    @Mock private RecordHistoryWriter recordHistoryWriter;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ApprovalService approvalService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        given(calculatedFieldEvaluator.recomputeCalculatedFields(any(), any()))
                .willAnswer(invocation -> invocation.getArgument(1));
    }

    private RecordRequest createRecordRequest(String data, UUID requesterId) {
        RecordRequest req = new RecordRequest();
        req.setData(data);
        req.setRequesterId(requesterId);
        req.setComment("테스트 요청");
        return req;
    }

    @Nested
    @DisplayName("requestRecordCreation")
    class RequestRecordCreation {

        @Test
        @DisplayName("성공 - 레코드 생성 결재를 요청한다")
        void success() {
            // given
            UUID nodeId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();
            UUID domainId = UUID.randomUUID();

            Domain domain = createTestDomain(domainId, "인사", "HR");
            domain.setIdentifierFieldId(UUID.randomUUID());
            domain.setDisplayNameFieldId(UUID.randomUUID());

            ClassificationNode node = createTestNode(nodeId, domain);

            RecordRequest request = createRecordRequest("{\"name\": \"test\"}", requesterId);

            DataQualityService.DQResult dqResult = new DataQualityService.DQResult();
            dqResult.isValid = true;

            Record savedRecord = new Record();
            savedRecord.setId(UUID.randomUUID());
            savedRecord.setNode(node);

            ApprovalRequest savedApproval = new ApprovalRequest();
            savedApproval.setId(UUID.randomUUID());
            savedApproval.setSteps(new ArrayList<>());

            MatchingService.DuplicateResult dupResult = new MatchingService.DuplicateResult();
            dupResult.hasDuplicates = false;
            dupResult.duplicateRecordIds = new ArrayList<>();

            given(nodeRepository.findById(nodeId)).willReturn(Optional.of(node));
            given(dqService.validateData(eq(nodeId), any(), any(), any())).willReturn(dqResult);
            given(fieldDefinitionService.getEffectiveFields(nodeId)).willReturn(Collections.emptyList());
            given(matchingService.checkDuplicates(eq(nodeId), any())).willReturn(dupResult);
            given(recordRepository.save(any(Record.class))).willReturn(savedRecord);
            given(workflowConfigRepository.findByNodeIdAndActionType(any(), eq("CREATE"))).willReturn(Collections.emptyList());
            given(workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(any(), eq("CREATE"))).willReturn(Collections.emptyList());
            given(approvalRepository.saveAndFlush(any(ApprovalRequest.class))).willReturn(savedApproval);

            // when
            ApprovalRequest result = approvalService.requestRecordCreation(nodeId, request);

            // then
            assertThat(result).isNotNull();
            verify(recordRepository).save(any(Record.class));
            verify(approvalRepository).saveAndFlush(any(ApprovalRequest.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 노드 ID로 요청 시 예외 발생")
        void failNodeNotFound() {
            // given
            UUID nodeId = UUID.randomUUID();
            RecordRequest request = createRecordRequest("{}", UUID.randomUUID());
            given(nodeRepository.findById(nodeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> approvalService.requestRecordCreation(nodeId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Node not found");
        }

        @Test
        @DisplayName("실패 - 필수 필드 매핑 누락 시 예외 발생")
        void failMissingFieldMapping() {
            // given
            UUID nodeId = UUID.randomUUID();
            Domain domain = createTestDomain(UUID.randomUUID(), "인사", "HR");
            domain.setIdentifierFieldId(null);
            domain.setDisplayNameFieldId(null);
            ClassificationNode node = createTestNode(nodeId, domain);

            RecordRequest request = createRecordRequest("{}", UUID.randomUUID());
            given(nodeRepository.findById(nodeId)).willReturn(Optional.of(node));

            // when & then
            assertThatThrownBy(() -> approvalService.requestRecordCreation(nodeId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("missing required field mappings");
        }

        @Test
        @DisplayName("실패 - 데이터 품질 검증 실패 시 예외 발생")
        void failDQValidation() {
            // given
            UUID nodeId = UUID.randomUUID();
            Domain domain = createTestDomain(UUID.randomUUID(), "인사", "HR");
            domain.setIdentifierFieldId(UUID.randomUUID());
            domain.setDisplayNameFieldId(UUID.randomUUID());
            ClassificationNode node = createTestNode(nodeId, domain);

            RecordRequest request = createRecordRequest("{}", UUID.randomUUID());

            DataQualityService.DQResult dqResult = new DataQualityService.DQResult();
            dqResult.isValid = false;
            dqResult.errors.add("Field 'name' is required.");

            given(nodeRepository.findById(nodeId)).willReturn(Optional.of(node));
            given(dqService.validateData(eq(nodeId), any(), any(), any())).willReturn(dqResult);

            // when & then
            assertThatThrownBy(() -> approvalService.requestRecordCreation(nodeId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Data Quality Check Failed");
        }
    }

    @Nested
    @DisplayName("approveStep")
    class ApproveStep {

        @Test
        @DisplayName("실패 - 본인이 아닌 사용자가 승인 시 예외 발생")
        void failNotAssignee() {
            // given
            UUID stepId = UUID.randomUUID();
            UUID assigneeId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            ApprovalStep step = new ApprovalStep();
            step.setId(stepId);
            step.setAssigneeId(assigneeId);
            step.setStatus("PENDING");

            given(stepRepository.findById(stepId)).willReturn(Optional.of(step));

            // when & then
            assertThatThrownBy(() -> approvalService.approveStep(stepId, otherUserId, "승인"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not the assignee");
        }

        @Test
        @DisplayName("실패 - 이미 처리된 단계 승인 시 예외 발생")
        void failAlreadyProcessed() {
            // given
            UUID stepId = UUID.randomUUID();
            UUID assigneeId = UUID.randomUUID();

            ApprovalStep step = new ApprovalStep();
            step.setId(stepId);
            step.setAssigneeId(assigneeId);
            step.setStatus("APPROVED");

            given(stepRepository.findById(stepId)).willReturn(Optional.of(step));

            // when & then
            assertThatThrownBy(() -> approvalService.approveStep(stepId, assigneeId, "승인"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not pending");
        }
    }

    @Nested
    @DisplayName("rejectStep")
    class RejectStep {

        @Test
        @DisplayName("실패 - 본인이 아닌 사용자가 반려 시 예외 발생")
        void failNotAssignee() {
            // given
            UUID stepId = UUID.randomUUID();
            UUID assigneeId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            ApprovalStep step = new ApprovalStep();
            step.setId(stepId);
            step.setAssigneeId(assigneeId);
            step.setStatus("PENDING");

            given(stepRepository.findById(stepId)).willReturn(Optional.of(step));

            // when & then
            assertThatThrownBy(() -> approvalService.rejectStep(stepId, otherUserId, "반려"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not the assignee");
        }

        @Test
        @DisplayName("성공 - 삭제 요청(RECORD_DELETE) 반려 시 대상 레코드를 ACTIVE 상태로 복구한다")
        void successRejectDeleteRequest() {
            // given
            UUID stepId = UUID.randomUUID();
            UUID assigneeId = UUID.randomUUID();
            UUID recordId = UUID.randomUUID();

            ApprovalRequest approval = new ApprovalRequest();
            approval.setId(UUID.randomUUID());
            approval.setTargetType("RECORD_DELETE");
            approval.setTargetId(recordId);

            ApprovalStep step = new ApprovalStep();
            step.setId(stepId);
            step.setAssigneeId(assigneeId);
            step.setStatus("PENDING");
            step.setApprovalRequest(approval);

            Record record = new Record();
            record.setId(recordId);
            record.setStatus("PENDING_APPROVAL");

            given(stepRepository.findById(stepId)).willReturn(Optional.of(step));
            given(recordRepository.findById(recordId)).willReturn(Optional.of(record));

            // when
            ApprovalRequest result = approvalService.rejectStep(stepId, assigneeId, "반려 사유");

            // then
            assertThat(result.getStatus()).isEqualTo("REJECTED");
            assertThat(step.getStatus()).isEqualTo("REJECTED");
            assertThat(record.getStatus()).isEqualTo("ACTIVE");
            verify(stepRepository).saveAndFlush(step);
            verify(approvalRepository).saveAndFlush(approval);
            verify(recordRepository).saveAndFlush(record);
        }
    }

    @Nested
    @DisplayName("approveStepEvent")
    class ApproveStepEvent {

        @Test
        @DisplayName("성공 - 결재 단계를 승인하고 승인 이벤트를 퍼블리싱한다")
        void successApproveAndPublishEvent() {
            // given
            UUID stepId = UUID.randomUUID();
            UUID assigneeId = UUID.randomUUID();

            ApprovalRequest approval = new ApprovalRequest();
            approval.setId(UUID.randomUUID());

            ApprovalStep step = new ApprovalStep();
            step.setId(stepId);
            step.setAssigneeId(assigneeId);
            step.setStatus("PENDING");
            step.setApprovalRequest(approval);

            given(stepRepository.findById(stepId)).willReturn(Optional.of(step));

            // when
            ApprovalRequest result = approvalService.approveStep(stepId, assigneeId, "승인 완료");

            // then
            assertThat(result).isNotNull();
            assertThat(step.getStatus()).isEqualTo("APPROVED");
            verify(stepRepository).saveAndFlush(step);
            verify(eventPublisher).publishEvent(any(ApprovalStepApprovedEvent.class));
        }
    }

    @Nested
    @DisplayName("requestRecordUpdate - DQ recordId 연동")
    class RequestRecordUpdateDqTest {

        @Test
        @DisplayName("성공 - 레코드 수정 요청 시 recordId를 dqService.validateData에 전달하여 자기 자신 중복 오진 방지")
        void passesRecordIdToDqService() {
            UUID recordId = UUID.randomUUID();
            UUID nodeId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            Domain domain = new Domain();
            domain.setId(UUID.randomUUID());

            ClassificationNode node = new ClassificationNode();
            node.setId(nodeId);

            Record record = new Record();
            record.setId(recordId);
            record.setStatus("ACTIVE");
            record.setNode(node);

            RecordRequest request = new RecordRequest();
            request.setData("{\"emp_id\":\"TEST\"}");
            request.setRequesterId(requesterId);
            request.setComment("수정 요청");

            DataQualityService.DQResult dqResult = new DataQualityService.DQResult();
            dqResult.isValid = true;

            MatchingService.DuplicateResult dupResult = new MatchingService.DuplicateResult();
            dupResult.hasDuplicates = false;

            given(recordRepository.findById(recordId)).willReturn(Optional.of(record));
            org.mockito.Mockito.lenient().when(dqService.validateData(any(), any(), any(), any())).thenReturn(dqResult);
            org.mockito.Mockito.lenient().when(dqService.validateData(any(), any(), any())).thenReturn(dqResult);
            org.mockito.Mockito.lenient().when(dqService.validateData(any(), any())).thenReturn(dqResult);
            given(matchingService.checkDuplicates(eq(nodeId), any())).willReturn(dupResult);
            given(approvalRepository.findByTargetIdAndStatus(eq(recordId), eq("PENDING"))).willReturn(Collections.emptyList());

            WorkflowConfig config = new WorkflowConfig();
            given(workflowConfigRepository.findByNodeIdAndActionType(eq(nodeId), eq("UPDATE"))).willReturn(List.of(config));

            ApprovalRequest savedApproval = new ApprovalRequest();
            savedApproval.setId(UUID.randomUUID());
            savedApproval.setStatus("PENDING");
            given(approvalRepository.saveAndFlush(any(ApprovalRequest.class))).willReturn(savedApproval);

            ApprovalRequest result = approvalService.requestRecordUpdate(recordId, request);

            assertThat(result).isNotNull();
            verify(dqService).validateData(eq(nodeId), eq("{\"emp_id\":\"TEST\"}"), eq(recordId), any());
        }
    }

    @Nested
    @DisplayName("requestRecordDeletion reference integrity")
    class RequestRecordDeletionReferenceIntegrityTest {

        @Test
        @DisplayName("blocks deletion when another record references the target through a domain reference field")
        void blocksDeletionWhenReferenced() {
            UUID recordId = UUID.randomUUID();
            Record target = new Record();
            target.setId(recordId);
            target.setStatus("ACTIVE");

            FieldDefinition referenceField = new FieldDefinition();
            referenceField.setKey("parentRecordId");
            Record referrer = new Record();
            referrer.setId(UUID.randomUUID());

            given(recordRepository.findById(recordId)).willReturn(Optional.of(target));
            given(fieldDefinitionRepository.findByType("DOMAIN_REFERENCE")).willReturn(List.of(referenceField));
            given(recordRepository.findReferencingRecords("parentRecordId", recordId.toString(), recordId))
                    .willReturn(List.of(referrer));

            assertThatThrownBy(() -> approvalService.requestRecordDeletion(recordId, createRecordRequest("{}", UUID.randomUUID())))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(ErrorCode.RECORD_REFERENCED_BY_OTHERS);

            verify(approvalRepository, org.mockito.Mockito.never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("allows deletion request when no records reference the target")
        void allowsDeletionWhenUnreferenced() {
            UUID recordId = UUID.randomUUID();
            UUID nodeId = UUID.randomUUID();
            ClassificationNode node = new ClassificationNode();
            node.setId(nodeId);
            node.setDomain(createTestDomain(UUID.randomUUID(), "domain", "DOMAIN"));
            Record target = new Record();
            target.setId(recordId);
            target.setNode(node);
            target.setStatus("ACTIVE");
            ApprovalRequest savedApproval = new ApprovalRequest();
            savedApproval.setSteps(new ArrayList<>());

            given(recordRepository.findById(recordId)).willReturn(Optional.of(target));
            given(fieldDefinitionRepository.findByType("DOMAIN_REFERENCE")).willReturn(Collections.emptyList());
            given(workflowConfigRepository.findByNodeIdAndActionType(nodeId, "DELETE")).willReturn(Collections.emptyList());
            given(workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(any(), eq("DELETE"))).willReturn(Collections.emptyList());
            given(approvalRepository.saveAndFlush(any(ApprovalRequest.class))).willReturn(savedApproval);

            ApprovalRequest result = approvalService.requestRecordDeletion(recordId, createRecordRequest("{}", UUID.randomUUID()));

            assertThat(result).isSameAs(savedApproval);
            verify(approvalRepository).saveAndFlush(any(ApprovalRequest.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 다단계 승인선 및 사용자/필드 권한 TDD 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("다단계 승인선 및 사용자/필드 권한 검증")
    class MultiStepApprovalAndPermissionTest {

        @Test
        @DisplayName("approvalLine JSON 구성 시 1차(PENDING), 2차(WAITING) 승인 스텝이 순차 생성된다")
        void multiStepApprovalLine_CreatesSequentialSteps() {
            UUID nodeId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();
            UUID approver1 = UUID.randomUUID();

            Domain domain = createTestDomain(UUID.randomUUID(), "domain", "DOMAIN");
            domain.setIdentifierFieldId(UUID.randomUUID());
            domain.setDisplayNameFieldId(UUID.randomUUID());

            ClassificationNode node = new ClassificationNode();
            node.setId(nodeId);
            node.setDomain(domain);

            WorkflowConfig config = new WorkflowConfig();
            config.setNodeId(nodeId);
            config.setActionType("CREATE");
            config.setStepsConfig("{"
                + "\"approvalLine\":["
                + "  {\"stepOrder\":1,\"stepName\":\"1차 승인\",\"assigneeType\":\"USER\",\"assigneeId\":\"" + approver1 + "\"},"
                + "  {\"stepOrder\":2,\"stepName\":\"2차 관리자 승인\",\"assigneeType\":\"ROLE\",\"assigneeRole\":\"ROLE_ADMIN\"}"
                + "]"
                + "}");

            given(nodeRepository.findById(nodeId)).willReturn(Optional.of(node));
            given(workflowConfigRepository.findByNodeIdAndActionType(nodeId, "CREATE")).willReturn(List.of(config));
            given(dqService.validateData(any(), any(), any(), any())).willReturn(new DataQualityService.DQResult() {{ isValid = true; }});
            given(matchingService.checkDuplicates(eq(nodeId), any())).willReturn(new MatchingService.DuplicateResult());
            given(recordRepository.save(any(Record.class))).willAnswer(inv -> { Record r = inv.getArgument(0); r.setId(UUID.randomUUID()); return r; });
            given(approvalRepository.saveAndFlush(any(ApprovalRequest.class))).willAnswer(inv -> inv.getArgument(0));

            RecordRequest req = createRecordRequest("{\"name\":\"test\"}", requesterId);
            ApprovalRequest result = approvalService.requestRecordCreation(nodeId, req);

            assertThat(result.getSteps()).hasSize(3);
            assertThat(result.getSteps().get(0).getStepOrder()).isEqualTo(1);
            assertThat(result.getSteps().get(0).getStatus()).isEqualTo("PENDING");
            assertThat(result.getSteps().get(0).getAssigneeId()).isEqualTo(approver1);

            assertThat(result.getSteps().get(1).getStepOrder()).isEqualTo(2);
            assertThat(result.getSteps().get(1).getStatus()).isEqualTo("WAITING");
            assertThat(result.getSteps().get(1).getAssigneeRole()).isEqualTo("ROLE_ADMIN");

            assertThat(result.getSteps().get(2).getStepOrder()).isEqualTo(0);
            assertThat(result.getSteps().get(2).getStepType()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("특정 사용자가 CREATE 권한만 설정된 경우 UPDATE 요청 시 ACCESS_DENIED 예외 발생")
        void userWithCreateOnlyPermission_CannotPerformUpdateAction() {
            UUID recordId = UUID.randomUUID();
            UUID nodeId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            ClassificationNode node = new ClassificationNode();
            node.setId(nodeId);

            Record record = new Record();
            record.setId(recordId);
            record.setStatus("ACTIVE");
            record.setNode(node);

            WorkflowConfig config = new WorkflowConfig();
            config.setNodeId(nodeId);
            config.setActionType("UPDATE");
            config.setStepsConfig("{"
                + "\"permissions\": ["
                + "  {\"targetType\":\"USER\",\"targetId\":\"" + requesterId + "\",\"allowedActions\":[\"CREATE\"]}"
                + "]"
                + "}");

            given(recordRepository.findById(recordId)).willReturn(Optional.of(record));
            given(nodeRepository.findById(nodeId)).willReturn(Optional.of(node));
            given(workflowConfigRepository.findByNodeIdAndActionType(eq(nodeId), eq("UPDATE"))).willReturn(List.of(config));

            RecordRequest req = new RecordRequest();
            req.setRequesterId(requesterId);
            req.setData("{\"emp_id\":\"TEST\"}");

            assertThatThrownBy(() -> approvalService.requestRecordUpdate(recordId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("행위 권한이 허용되지 않았습니다");
        }

        @Test
        @DisplayName("username으로 등록된 워크플로우 자격 매칭 검증 성공")
        void validateUserActionPermission_MatchingByUsername_Success() {
            UUID requesterId = UUID.randomUUID();
            String username = "profavor.user";

            com.classification.domain_system.entity.User mockUser = new com.classification.domain_system.entity.User();
            mockUser.setId(requesterId.toString());
            mockUser.setUsername(username);

            given(userRepository.findByUsername(username)).willReturn(Optional.of(mockUser));

            WorkflowConfig config = new WorkflowConfig();
            config.setStepsConfig("{"
                + "\"permissions\": ["
                + "  {\"targetType\":\"USER\",\"targetId\":\"" + username + "\",\"allowedActions\":[\"CREATE\"],\"editableFields\":[\"emp_id\"]}"
                + "]"
                + "}");

            // Should not throw exception because username profavor.user matches requesterId UUID
            assertThatCode(() -> approvalService.validateUserActionPermission(config, requesterId, null, "CREATE"))
                .doesNotThrowAnyException();

            List<String> editable = approvalService.extractEditableFields(config, requesterId, null);
            assertThat(editable).containsExactly("emp_id");
        }
    }
}
