package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.MatchCandidateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchCandidateControllerTest {

    @Mock
    private MatchCandidateService candidateService;

    @InjectMocks
    private MatchCandidateController matchCandidateController;

    private MatchCandidate createMockCandidate() {
        MatchCandidate candidate = new MatchCandidate();
        candidate.setId(UUID.randomUUID());
        candidate.setDomainId(UUID.randomUUID());
        candidate.setStatus("PENDING");
        return candidate;
    }

    @Test
    @DisplayName("도메인별 매칭 후보 페이징 조회 성공")
    void testGetCandidates_Success() {
        UUID domainId = UUID.randomUUID();
        MatchCandidate candidate = createMockCandidate();
        Page<MatchCandidate> page = new PageImpl<>(List.of(candidate));
        PageResponse<MatchCandidate> pageResponse = PageResponse.of(page);

        when(candidateService.getCandidatesByDomain(domainId, "PENDING", 0, 10))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<MatchCandidate>> response = matchCandidateController.getCandidates(domainId, "PENDING", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().totalElements());
    }

    @Test
    @DisplayName("전체 매칭 후보 페이징 조회 성공")
    void testGetAllCandidates_Success() {
        MatchCandidate candidate = createMockCandidate();
        Page<MatchCandidate> page = new PageImpl<>(List.of(candidate));
        PageResponse<MatchCandidate> pageResponse = PageResponse.of(page);

        when(candidateService.getAllCandidates("PENDING", 0, 10)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<MatchCandidate>> response = matchCandidateController.getAllCandidates("PENDING", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().totalElements());
    }

    @Test
    @DisplayName("매칭 후보 승인(확정) 성공")
    void testConfirmCandidate_Success() {
        UUID id = UUID.randomUUID();
        MatchCandidate candidate = createMockCandidate();
        candidate.setStatus("CONFIRMED_MERGE");

        when(candidateService.confirmCandidate(eq(id), any(), eq("admin"))).thenReturn(candidate);

        ResponseEntity<MatchCandidate> response = matchCandidateController.confirm(id, null, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONFIRMED_MERGE", response.getBody().getStatus());
    }

    @Test
    @DisplayName("매칭 후보 거절 성공")
    void testRejectCandidate_Success() {
        UUID id = UUID.randomUUID();
        Record record = new Record();
        record.setId(UUID.randomUUID());

        when(candidateService.rejectCandidate(eq(id), eq("admin"))).thenReturn(record);

        ResponseEntity<Record> response = matchCandidateController.reject(id, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("매칭 후보 무시 성공")
    void testIgnoreCandidate_Success() {
        UUID id = UUID.randomUUID();
        MatchCandidate candidate = createMockCandidate();
        candidate.setStatus("IGNORED");

        when(candidateService.ignoreCandidate(eq(id), eq("admin"))).thenReturn(candidate);

        ResponseEntity<MatchCandidate> response = matchCandidateController.ignore(id, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IGNORED", response.getBody().getStatus());
    }

    @Test
    @DisplayName("매칭 후보 일괄 승인 및 일괄 거절 성공")
    void testBatchOperations_Success() {
        UUID domainId = UUID.randomUUID();
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        MatchCandidateController.BatchRequest batchRequest = new MatchCandidateController.BatchRequest(ids, domainId);

        ResponseEntity<Void> confirmResponse = matchCandidateController.batchConfirm(batchRequest, "admin");
        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());
        verify(candidateService).batchConfirmCandidates(ids, domainId, "admin");

        ResponseEntity<Void> rejectResponse = matchCandidateController.batchReject(batchRequest, "admin");
        assertEquals(HttpStatus.OK, rejectResponse.getStatusCode());
        verify(candidateService).batchRejectCandidates(ids, "admin");
    }
}
