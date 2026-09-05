package com.classification.domain_system.service;

import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.dto.InboxMessageResponse;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.MatchCandidateRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchCandidateNotificationTest {

    @Mock
    private MatchCandidateRepository candidateRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private RecordMergeService recordMergeService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private InboxService inboxService;

    @Mock
    private UserRepository userRepository;

    private MatchCandidateService candidateService;

    private UUID nodeId;
    private UUID domainId;
    private UUID existingRecordId;
    private ClassificationNode node;
    private Domain domain;
    private Record existingRecord;

    @BeforeEach
    void setUp() {
        candidateService = new MatchCandidateService(
                candidateRepository,
                recordRepository,
                nodeRepository,
                recordMergeService,
                null,
                securityUtils,
                inboxService,
                userRepository
        );

        nodeId = UUID.randomUUID();
        domainId = UUID.randomUUID();
        existingRecordId = UUID.randomUUID();

        domain = new Domain();
        domain.setId(domainId);
        Map<String, String> domainNames = new HashMap<>();
        domainNames.put("ko", "상품 도메인");
        domain.setName(domainNames);

        node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);
        Map<String, String> nodeNames = new HashMap<>();
        nodeNames.put("ko", "가공식품");
        node.setName(nodeNames);

        existingRecord = new Record();
        existingRecord.setId(existingRecordId);
        existingRecord.setNode(node);
        existingRecord.setData("{\"PRODUCT_NAME\":\"신라면\",\"PRODUCT_ID\":\"1001\"}");
    }

    @Test
    @DisplayName("#148: 중복 후보 등록 시 PENDING_REVIEW 상태로 저장되고 스튜어드/관리자 인박스로 알림이 발송된다")
    void testCreateAndNotifyCandidate_SavesCandidateAndSendsInboxMessage() {
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));

        User adminUser = new User();
        adminUser.setId("admin");
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@mplatform.com");
        when(userRepository.findAll()).thenReturn(List.of(adminUser));

        when(candidateRepository.save(any(MatchCandidate.class))).thenAnswer(invocation -> {
            MatchCandidate c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
            return c;
        });

        String incomingJson = "{\"PRODUCT_NAME\":\"신라면 5입\",\"PRODUCT_ID\":\"1001\"}";
        MatchCandidate result = candidateService.createAndNotifyCandidate(
                nodeId,
                existingRecordId,
                incomingJson,
                0.95,
                "PRODUCT_ID 정확 일치",
                null,
                "MANUAL"
        );

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING_REVIEW");
        assertThat(result.getScore()).isEqualTo(0.95);
        assertThat(result.getNodeId()).isEqualTo(nodeId);
        assertThat(result.getExistingRecordId()).isEqualTo(existingRecordId);

        // Verify inbox notification
        ArgumentCaptor<InboxMessageRequest> reqCaptor = ArgumentCaptor.forClass(InboxMessageRequest.class);
        verify(inboxService, times(1)).sendMessage(reqCaptor.capture(), eq("system"));

        InboxMessageRequest sentReq = reqCaptor.getValue();
        assertThat(sentReq.getSubject()).contains("중복");
        assertThat(sentReq.getImportance()).isEqualTo("HIGH");
        assertThat(sentReq.getMessageType()).isEqualTo("MATCH_REVIEW");
        assertThat(sentReq.getToRecipients()).contains("admin");
        assertThat(sentReq.getBody()).contains("CAND-");
        assertThat(sentReq.getBody()).contains("REC-");
    }

    @Test
    @DisplayName("#148: InboxService가 비활성화되거나 예외가 발생해도 후보는 정상 저장된다")
    void testCreateAndNotifyCandidate_HandlesInboxExceptionGracefully() {
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        when(candidateRepository.save(any(MatchCandidate.class))).thenAnswer(invocation -> {
            MatchCandidate c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
            return c;
        });

        doThrow(new RuntimeException("Notification failure")).when(inboxService).sendMessage(any(), any());

        String incomingJson = "{\"PRODUCT_NAME\":\"신라면 5입\",\"PRODUCT_ID\":\"1001\"}";
        MatchCandidate result = candidateService.createAndNotifyCandidate(
                nodeId,
                existingRecordId,
                incomingJson,
                0.90,
                "PRODUCT_NAME 유사",
                null,
                "INBOUND"
        );

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING_REVIEW");
    }

    @Test
    @DisplayName("#148: 검토 승인(CONFIRMED_MERGE) 및 반려(REJECTED) 상태 전이 검증")
    void testConfirmAndRejectCandidate_UpdatesStatus() {
        UUID candidateId = UUID.randomUUID();
        MatchCandidate candidate = new MatchCandidate();
        candidate.setId(candidateId);
        candidate.setNodeId(nodeId);
        candidate.setExistingRecordId(existingRecordId);
        candidate.setStatus("PENDING_REVIEW");
        candidate.setIncomingDataJson("{\"PRODUCT_NAME\":\"신라면\"}");
        candidate.setSource("MANUAL");

        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(MatchCandidate.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchCandidate confirmed = candidateService.confirmCandidate(candidateId, null, "admin");
        assertThat(confirmed.getStatus()).isEqualTo("CONFIRMED_MERGE");
        assertThat(confirmed.getReviewedBy()).isEqualTo("admin");

        // Prepare for reject
        candidate.setStatus("PENDING_REVIEW");
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(recordRepository.save(any(Record.class))).thenAnswer(inv -> inv.getArgument(0));

        Record created = candidateService.rejectCandidate(candidateId, "admin");
        assertThat(created).isNotNull();
        assertThat(candidate.getStatus()).isEqualTo("REJECTED");
    }
}
