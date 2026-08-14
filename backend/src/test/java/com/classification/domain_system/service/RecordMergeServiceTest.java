package com.classification.domain_system.service;

import com.classification.domain_system.config.MdmProperties;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.SurvivorshipRule;
import com.classification.domain_system.entity.enums.RecordStatus;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    private UserRepository userRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    @Mock
    private DataQualityService dqService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private MdmProperties mdmProperties;

    @InjectMocks
    private RecordMergeService recordMergeService;

    private UUID survivorId;
    private UUID mergedId;
    private UUID domainId;
    private ClassificationNode node;
    private Record survivor;
    private Record merged;

    @BeforeEach
    void setUp() {
        survivorId = UUID.randomUUID();
        mergedId = UUID.randomUUID();
        domainId = UUID.randomUUID();

        node = new ClassificationNode();
        node.setId(domainId);

        survivor = new Record();
        survivor.setId(survivorId);
        survivor.setNode(node);
        survivor.setStatus(RecordStatus.ACTIVE.name());
        survivor.setData("{\"name\":\"Survivor Name\",\"code\":\"A001\"}");

        merged = new Record();
        merged.setId(mergedId);
        merged.setNode(node);
        merged.setStatus(RecordStatus.ACTIVE.name());
        merged.setData("{\"name\":\"Merged Name\",\"code\":\"A001\"}");
    }

    @Test
    @DisplayName("레코드 병합 성공: 생존 레코드 데이터 갱신 및 병합 레코드 MERGED 상태 변경")
    void mergeRecords_Success() {
        RecordMergeService.MergeRequest request = new RecordMergeService.MergeRequest();
        request.survivorRecordId = survivorId;
        request.mergedRecordIds = List.of(mergedId);
        request.fieldResolutions = Map.of("name", mergedId);

        given(recordRepository.findById(survivorId)).willReturn(Optional.of(survivor));
        given(recordRepository.findById(mergedId)).willReturn(Optional.of(merged));
        given(recordRepository.save(any(Record.class))).willAnswer(inv -> inv.getArgument(0));

        Record result = recordMergeService.mergeRecords(request, "admin");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(survivorId);
        assertThat(merged.getStatus()).isEqualTo(RecordStatus.MERGED.name());
        assertThat(merged.getMergedIntoRecordId()).isEqualTo(survivorId);
        verify(recordRepository).save(survivor);
        verify(recordRepository).save(merged);
    }

    @Test
    @DisplayName("이미 병합된 레코드를 Survivor로 지정 시 BusinessException 예외 발생")
    void mergeRecords_AlreadyMergedSurvivor_ThrowsException() {
        survivor.setStatus(RecordStatus.MERGED.name());

        RecordMergeService.MergeRequest request = new RecordMergeService.MergeRequest();
        request.survivorRecordId = survivorId;
        request.mergedRecordIds = List.of(mergedId);

        given(recordRepository.findById(survivorId)).willReturn(Optional.of(survivor));

        assertThatThrownBy(() -> recordMergeService.mergeRecords(request, "admin"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("생존성 규칙 조회 및 갱신 검증")
    void getAndUpdateSurvivorshipRules_Success() {
        SurvivorshipRule rule = new SurvivorshipRule();
        rule.setId(UUID.randomUUID());
        rule.setDomainId(domainId);
        rule.setFieldKey("name");
        rule.setStrategy("MOST_RECENT");
        rule.setPriority(1);

        given(survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId)).willReturn(List.of(rule));

        List<SurvivorshipRule> rules = recordMergeService.getSurvivorshipRules(domainId);
        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getFieldKey()).isEqualTo("name");

        recordMergeService.updateSurvivorshipRules(domainId, List.of(rule));
        verify(survivorshipRuleRepository).save(any(SurvivorshipRule.class));
    }

    @Test
    @DisplayName("레코드 병합 해제(Unmerge) 성공: 이전 스냅샷 복구 및 상태 ACTIVE 전환")
    void unmergeRecord_Success() {
        merged.setStatus(RecordStatus.MERGED.name());
        merged.setMergedIntoRecordId(survivorId);

        given(recordRepository.findById(mergedId)).willReturn(Optional.of(merged));
        given(recordRepository.save(any(Record.class))).willAnswer(inv -> inv.getArgument(0));

        Record unmerged = recordMergeService.unmergeRecord(mergedId, "admin");

        assertThat(unmerged).isNotNull();
        assertThat(unmerged.getStatus()).isEqualTo(RecordStatus.ACTIVE.name());
        assertThat(unmerged.getMergedIntoRecordId()).isNull();
    }
}
