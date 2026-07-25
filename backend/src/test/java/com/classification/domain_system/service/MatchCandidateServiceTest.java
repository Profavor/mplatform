package com.classification.domain_system.service;

import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.MatchCandidateRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchCandidateServiceTest {

    @Mock
    private MatchCandidateRepository candidateRepository;
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private ClassificationNodeRepository nodeRepository;
    @Mock
    private RecordMergeService recordMergeService;

    @InjectMocks
    private MatchCandidateService matchCandidateService;

    private UUID candidateId;
    private MatchCandidate candidate;

    @BeforeEach
    void setUp() {
        candidateId = UUID.randomUUID();
        candidate = new MatchCandidate();
        candidate.setId(candidateId);
        candidate.setStatus("PENDING_REVIEW");
        candidate.setScore(0.88);
    }


    @Test
    @DisplayName("confirmCandidate - 중복 후보를 병합 승인 상태(CONFIRMED_MERGE)로 변경")
    void confirmCandidate_Success() {
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(MatchCandidate.class))).thenAnswer(i -> i.getArgument(0));

        MatchCandidate result = matchCandidateService.confirmCandidate(candidateId, null, "steward1");

        assertThat(result.getStatus()).isEqualTo("CONFIRMED_MERGE");
        assertThat(result.getReviewedBy()).isEqualTo("steward1");
        verify(candidateRepository, times(1)).save(any(MatchCandidate.class));
    }

    @Test
    @DisplayName("ignoreCandidate - 중복 후보를 별도 유지 상태(IGNORED)로 변경")
    void ignoreCandidate_Success() {
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(MatchCandidate.class))).thenAnswer(i -> i.getArgument(0));

        MatchCandidate result = matchCandidateService.ignoreCandidate(candidateId, "steward1");

        assertThat(result.getStatus()).isEqualTo("IGNORED");
        assertThat(result.getReviewedBy()).isEqualTo("steward1");
        verify(candidateRepository, times(1)).save(any(MatchCandidate.class));
    }
}
