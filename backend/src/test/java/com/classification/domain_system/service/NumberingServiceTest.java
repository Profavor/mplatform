package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class NumberingServiceTest extends BaseServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private NumberingService numberingService;

    @Nested
    @DisplayName("generateCode")
    class GenerateCode {

        @Test
        @DisplayName("성공 - 패턴 ITEM-{YYYY}-{SEQ:4}에 의거해 코드를 포매팅한다")
        void successSimplePattern() {
            // given
            Domain domain = new Domain();
            domain.setId(UUID.randomUUID());
            domain.setNumberingPattern("ITEM-{YYYY}-{SEQ:4}");
            domain.setCurrentSequence(1L);

            // when
            String code = numberingService.generateCode(domain);

            // then
            assertThat(code).startsWith("ITEM-");
            // Check that the sequence is padded with 4 zeros: "0001"
            assertThat(code).endsWith("0001");
            assertThat(code).hasSize(14); // "ITEM-2026-0001"
        }

        @Test
        @DisplayName("성공 - {MM} 및 {DD} 날짜 변환자도 치환한다")
        void successDateTokens() {
            // given
            Domain domain = new Domain();
            domain.setId(UUID.randomUUID());
            domain.setNumberingPattern("DEPT-{YYYY}{MM}{DD}-{SEQ:3}");
            domain.setCurrentSequence(15L);

            // when
            String code = numberingService.generateCode(domain);

            // then
            assertThat(code).startsWith("DEPT-");
            assertThat(code).endsWith("015");
            assertThat(code).contains(String.valueOf(java.time.LocalDate.now().getYear()));
        }
    }

    @Nested
    @DisplayName("issueNextCode")
    class IssueNextCode {

        @Test
        @DisplayName("성공 - 도메인 시퀀스를 안전하게 1 증가시키고 새 코드를 발급 및 저장한다")
        void successSequenceIncrement() {
            // given
            UUID domainId = UUID.randomUUID();
            Domain domain = new Domain();
            domain.setId(domainId);
            domain.setNumberingPattern("PRD-{SEQ:5}");
            domain.setCurrentSequence(10L);

            given(domainRepository.findWithLockById(domainId)).willReturn(Optional.of(domain));
            given(domainRepository.save(any(Domain.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            String code = numberingService.issueNextCode(domainId);

            // then
            assertThat(code).isEqualTo("PRD-00011");
            assertThat(domain.getCurrentSequence()).isEqualTo(11L);
            verify(domainRepository).save(domain);
        }
    }
}
