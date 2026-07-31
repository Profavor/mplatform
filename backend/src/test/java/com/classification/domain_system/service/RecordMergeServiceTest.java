package com.classification.domain_system.service;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.event.MasterDataChangedEvent;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordMergeServiceTest {

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private RecordHistoryRepository recordHistoryRepository;
    @Mock
    private SurvivorshipRuleRepository survivorshipRuleRepository;
    @Mock
    private SourcePriorityRepository sourcePriorityRepository;
    @Mock
    private RecordFieldSourceRepository recordFieldSourceRepository;
    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock
    private DataQualityService dqService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private RecordMergeService recordMergeService;

    private UUID domainId;
    private UUID nodeId;
    private UUID survivorId;
    private UUID mergedId;
    private ClassificationNode node;
    private Domain domain;
    private Record survivor;
    private Record merged;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        nodeId = UUID.randomUUID();
        survivorId = UUID.randomUUID();
        mergedId = UUID.randomUUID();

        domain = new Domain();
        domain.setId(domainId);

        node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);

        survivor = new Record();
        survivor.setId(survivorId);
        survivor.setNode(node);
        survivor.setStatus("ACTIVE");
        survivor.setSourceSystem("SYS_B");
        survivor.setData("{\"name\":\"삼성전자\", \"code\":\"005930\"}");
        survivor.setVersion(1);

        merged = new Record();
        merged.setId(mergedId);
        merged.setNode(node);
        merged.setStatus("ACTIVE");
        merged.setSourceSystem("SYS_A");
        merged.setData("{\"name\":\"(주)삼성전자 주식회사\", \"code\":\"005930_OLD\", \"phone\":\"02-123-4567\"}");
        merged.setVersion(1);
    }

    @Test
    @DisplayName("mergeRecords - survivor와 merged 레코드가 성공적으로 병합되고 merged 상태로 변경됨")
    void mergeRecords_Success() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);
        req.fieldResolutions = Map.of("phone", mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record result = recordMergeService.mergeRecords(req, "admin");

        assertThat(result.getId()).isEqualTo(survivorId);
        assertThat(result.getData()).contains("phone");
        assertThat(merged.getStatus()).isEqualTo("MERGED");
        assertThat(merged.getMergedIntoRecordId()).isEqualTo(survivorId);
        verify(recordHistoryRepository, atLeastOnce()).save(any(RecordHistory.class));
    }

    @Test
    @DisplayName("P0-1: mergeWithSurvivorship - SOURCE_PRIORITY 전략으로 소스 우선순위 높은 레코드 값 채택")
    void mergeWithSurvivorship_SourcePriority() {
        SurvivorshipRule rule = new SurvivorshipRule();
        rule.setDomainId(domainId);
        rule.setFieldKey("name");
        rule.setStrategy("SOURCE_PRIORITY");
        rule.setPriority(1);

        SourcePriority sp1 = new SourcePriority();
        sp1.setDomainId(domainId);
        sp1.setSourceSystem("SYS_A");
        sp1.setPriority(1); // 우선순위 1 (더 상위)

        SourcePriority sp2 = new SourcePriority();
        sp2.setDomainId(domainId);
        sp2.setSourceSystem("SYS_B");
        sp2.setPriority(2);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId)).thenReturn(List.of(rule));
        when(sourcePriorityRepository.findByDomainIdOrderByPriorityAsc(domainId)).thenReturn(List.of(sp1, sp2));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record result = recordMergeService.mergeWithSurvivorship(survivorId, List.of(mergedId), "admin");

        assertThat(result.getData()).contains("\"name\":\"(주)삼성전자 주식회사\"");
    }

    @Test
    @DisplayName("P0-1: mergeWithSurvivorship - MOST_RECENT 전략으로 최신 수정 레코드 값 채택")
    void mergeWithSurvivorship_MostRecent() {
        SurvivorshipRule rule = new SurvivorshipRule();
        rule.setDomainId(domainId);
        rule.setFieldKey("name");
        rule.setStrategy("MOST_RECENT");
        rule.setPriority(1);

        RecordFieldSource rfsMerged = new RecordFieldSource();
        rfsMerged.setRecordId(mergedId);
        rfsMerged.setFieldKey("name");
        rfsMerged.setSourceSystem("SYS_A");
        rfsMerged.setUpdatedAt(LocalDateTime.now().plusHours(2));

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId)).thenReturn(List.of(rule));
        lenient().when(recordFieldSourceRepository.findByRecordIdAndFieldKey(mergedId, "name")).thenReturn(Optional.of(rfsMerged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record result = recordMergeService.mergeWithSurvivorship(survivorId, List.of(mergedId), "admin");

        assertThat(result.getData()).contains("\"name\":\"(주)삼성전자 주식회사\"");
    }

    @Test
    @DisplayName("P0-1: mergeWithSurvivorship - MOST_COMPLETE 전략으로 더 완전한(길이가 긴) 값 채택")
    void mergeWithSurvivorship_MostComplete() {
        SurvivorshipRule rule = new SurvivorshipRule();
        rule.setDomainId(domainId);
        rule.setFieldKey("name");
        rule.setStrategy("MOST_COMPLETE");
        rule.setPriority(1);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId)).thenReturn(List.of(rule));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record result = recordMergeService.mergeWithSurvivorship(survivorId, List.of(mergedId), "admin");

        assertThat(result.getData()).contains("\"name\":\"(주)삼성전자 주식회사\"");
    }

    @Test
    @DisplayName("P0-1: mergeWithSurvivorship - 규칙이 없는 필드는 survivor 값이 그대로 유지됨")
    void mergeWithSurvivorship_UndefinedRuleFieldPreserved() {
        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId)).thenReturn(Collections.emptyList());
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record result = recordMergeService.mergeWithSurvivorship(survivorId, List.of(mergedId), "admin");

        assertThat(result.getData()).contains("\"name\":\"삼성전자\"");
    }

    @Test
    @DisplayName("P0-2: mergeRecords - merged 레코드를 가리키는 DOMAIN_REFERENCE 필드가 survivor ID로 갱신되고 history 남김")
    void mergeRecords_RepointsReferences() {
        FieldDefinition refField = new FieldDefinition();
        refField.setKey("companyId");
        refField.setType("DOMAIN_REFERENCE");

        Record referencingRecord = new Record();
        referencingRecord.setId(UUID.randomUUID());
        referencingRecord.setNode(node);
        referencingRecord.setData("{\"companyId\":\"" + mergedId.toString() + "\", \"title\":\"Employee A\"}");
        referencingRecord.setVersion(1);

        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(fieldDefinitionRepository.findByType("DOMAIN_REFERENCE")).thenReturn(List.of(refField));
        when(recordRepository.findReferencingRecords("companyId", mergedId.toString(), mergedId)).thenReturn(List.of(referencingRecord));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        recordMergeService.mergeRecords(req, "admin");

        assertThat(referencingRecord.getData()).contains(survivorId.toString());

        ArgumentCaptor<RecordHistory> historyCaptor = ArgumentCaptor.forClass(RecordHistory.class);
        verify(recordHistoryRepository, atLeastOnce()).save(historyCaptor.capture());

        boolean hasRepointedHistory = historyCaptor.getAllValues().stream()
                .anyMatch(h -> "REFERENCE_REPOINTED".equals(h.getChangeType()) && h.getRecordId().equals(referencingRecord.getId()));
        assertThat(hasRepointedHistory).isTrue();
    }

    @Test
    @DisplayName("P0-3: mergeRecords 및 unmergeRecord - MasterDataChangedEvent 이벤트 발행 검증")
    void mergeRecords_PublishesMasterDataChangedEvent() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        recordMergeService.mergeRecords(req, "admin");

        verify(applicationEventPublisher, times(2)).publishEvent(any(MasterDataChangedEvent.class));
    }

    @Test
    @DisplayName("P0-4: mergeRecords 및 unmergeRecord - Audit 정보 changedBy 저장 검증")
    void mergeRecords_AuditChangedBy() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        String operator = "operator_user";
        UUID expectedUuid = UUID.nameUUIDFromBytes(operator.getBytes(StandardCharsets.UTF_8));

        recordMergeService.mergeRecords(req, operator);

        ArgumentCaptor<RecordHistory> historyCaptor = ArgumentCaptor.forClass(RecordHistory.class);
        verify(recordHistoryRepository, atLeastOnce()).save(historyCaptor.capture());

        assertThat(historyCaptor.getAllValues()).allMatch(h -> operator.equals(h.getChangedBy()));
    }

    @Test
    @DisplayName("P1-1: mergeRecords - fieldResolutions로 선택된 필드의 RecordFieldSource.sourceSystem 갱신 검증")
    void mergeRecords_UpdatesRecordFieldSource() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);
        req.fieldResolutions = Map.of("phone", mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        recordMergeService.mergeRecords(req, "admin");

        ArgumentCaptor<RecordFieldSource> rfsCaptor = ArgumentCaptor.forClass(RecordFieldSource.class);
        verify(recordFieldSourceRepository, atLeastOnce()).save(rfsCaptor.capture());

        Optional<RecordFieldSource> phoneRfs = rfsCaptor.getAllValues().stream()
                .filter(rfs -> "phone".equals(rfs.getFieldKey()))
                .findFirst();

        assertThat(phoneRfs).isPresent();
        assertThat(phoneRfs.get().getSourceSystem()).isEqualTo("SYS_A");
    }

    @Test
    @DisplayName("P1-2: unmergeRecord - 언머지 시 survivor 데이터가 병합 이전 상태로 복원됨 (UNMERGE_SURVIVOR_ROLLBACK)")
    void unmergeRecord_RollsBackSurvivorData() {
        merged.setStatus("MERGED");
        merged.setMergedIntoRecordId(survivorId);

        String previousDataBeforeMerge = "{\"name\":\"삼성전자\", \"code\":\"005930\"}";
        survivor.setData("{\"name\":\"(주)삼성전자\", \"code\":\"005930\", \"phone\":\"02-123-4567\"}");

        RecordHistory mergeHistory = new RecordHistory();
        mergeHistory.setRecordId(survivorId);
        mergeHistory.setChangeType("MERGE_SURVIVOR");
        mergeHistory.setPreviousData(previousDataBeforeMerge);
        mergeHistory.setNewData(survivor.getData());

        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordHistoryRepository.findByRecordIdOrderByChangedAtDesc(survivorId)).thenReturn(List.of(mergeHistory));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        recordMergeService.unmergeRecord(mergedId, "admin");

        assertThat(merged.getStatus()).isEqualTo("ACTIVE");
        assertThat(merged.getMergedIntoRecordId()).isNull();
        assertThat(survivor.getData()).isEqualTo(previousDataBeforeMerge);

        ArgumentCaptor<RecordHistory> historyCaptor = ArgumentCaptor.forClass(RecordHistory.class);
        verify(recordHistoryRepository, atLeastOnce()).save(historyCaptor.capture());

        boolean hasRollbackHistory = historyCaptor.getAllValues().stream()
                .anyMatch(h -> "UNMERGE_SURVIVOR_ROLLBACK".equals(h.getChangeType()) && survivorId.equals(h.getRecordId()));
        assertThat(hasRollbackHistory).isTrue();
    }

    @Test
    @DisplayName("P1-3: mergeRecords - 서로 다른 노드의 레코드를 병합 시도 시 BusinessException 발생")
    void mergeRecords_DifferentNode_ThrowsException() {
        ClassificationNode otherNode = new ClassificationNode();
        otherNode.setId(UUID.randomUUID());
        otherNode.setDomain(domain);

        Record otherRecord = new Record();
        otherRecord.setId(UUID.randomUUID());
        otherRecord.setNode(otherNode);
        otherRecord.setStatus("ACTIVE");

        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(otherRecord.getId());

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(otherRecord.getId())).thenReturn(Optional.of(otherRecord));

        assertThatThrownBy(() -> recordMergeService.mergeRecords(req, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot merge records from a different node/domain.");
    }

    @Test
    @DisplayName("P1: mergeRecords - DQ 검증 실패 시 DATA_QUALITY_CHECK_FAILED 예외 발생 및 병합 중단")
    void mergeRecords_DqValidationFailed_ThrowsException() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));

        DataQualityService.DQResult dqResult = new DataQualityService.DQResult();
        dqResult.isValid = false;
        dqResult.errors = List.of("필수 필드가 누락되었습니다.");
        when(dqService.validateData(eq(nodeId), any(), eq(survivorId), eq(null))).thenReturn(dqResult);

        assertThatThrownBy(() -> recordMergeService.mergeRecords(req, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("필수 필드가 누락되었습니다.");
    }

    @Test
    @DisplayName("P2: mergeRecords - 병합 성공 시 NotificationService.createNotification이 호출된다")
    void mergeRecords_NotificationSent() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        recordMergeService.mergeRecords(req, "admin");

        verify(notificationService).createNotification(any(), eq("레코드 병합 안내"), any(), eq("RECORD_MERGE"), any());
    }

    @Test
    @DisplayName("P2: unmergeRecord - 언머지 성공 시 NotificationService.createNotification이 호출된다")
    void unmergeRecord_NotificationSent() {
        merged.setStatus("MERGED");
        merged.setMergedIntoRecordId(survivorId);

        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        recordMergeService.unmergeRecord(mergedId, "admin");

        verify(notificationService).createNotification(any(), eq("레코드 병합 해제 안내"), any(), eq("RECORD_UNMERGE"), any());
    }
}
