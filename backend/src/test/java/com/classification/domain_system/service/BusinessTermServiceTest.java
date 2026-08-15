package com.classification.domain_system.service;

import com.classification.domain_system.dto.BusinessTermDto;
import com.classification.domain_system.entity.BusinessTerm;
import com.classification.domain_system.repository.BusinessTermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BusinessTermServiceTest {

    @Mock private BusinessTermRepository businessTermRepository;

    @InjectMocks
    private BusinessTermService businessTermService;

    private BusinessTerm term1;
    private BusinessTerm term2;

    @BeforeEach
    void setUp() {
        term1 = BusinessTerm.builder()
                .id(UUID.randomUUID())
                .termName(Map.of("ko", "사업자등록번호", "en", "Business Registration Number"))
                .termCode("BIZ_NO")
                .abbreviation("BRN")
                .synonyms("사업자번호,등록번호")
                .dataType("STRING")
                .sensitivityLevel("SENSITIVE")
                .description("국세청 부여 사업자 식별 번호")
                .build();

        term2 = BusinessTerm.builder()
                .id(UUID.randomUUID())
                .termName(Map.of("ko", "고객명", "en", "Customer Name"))
                .termCode("CUST_NM")
                .abbreviation("NM")
                .synonyms("성명,거래처명")
                .dataType("STRING")
                .sensitivityLevel("GENERAL")
                .build();
    }

    @Test
    @DisplayName("createTerm: 표준 용어 생성 및 반환 검증")
    void testCreateTerm() {
        when(businessTermRepository.save(any(BusinessTerm.class))).thenReturn(term1);

        BusinessTermDto.BusinessTermCreateRequest request = BusinessTermDto.BusinessTermCreateRequest.builder()
                .termName(Map.of("ko", "사업자등록번호"))
                .termCode("BIZ_NO")
                .abbreviation("BRN")
                .dataType("STRING")
                .sensitivityLevel("SENSITIVE")
                .build();

        BusinessTermDto.BusinessTermResponse res = businessTermService.createTerm(request);

        assertThat(res).isNotNull();
        assertThat(res.getTermCode()).isEqualTo("BIZ_NO");
        assertThat(res.getSensitivityLevel()).isEqualTo("SENSITIVE");
    }

    @Test
    @DisplayName("recommendTerms: 키워드 기반 표준 용어 추천 및 점수 순위 검증")
    void testRecommendTerms() {
        when(businessTermRepository.findAll()).thenReturn(List.of(term1, term2));

        // 1. Exact match
        List<BusinessTermDto.TermRecommendation> rec1 = businessTermService.recommendTerms("BIZ_NO");
        assertThat(rec1).isNotEmpty();
        assertThat(rec1.get(0).getTerm().getTermCode()).isEqualTo("BIZ_NO");
        assertThat(rec1.get(0).getSimilarityScore()).isEqualTo(1.0);

        // 2. Synonym match
        List<BusinessTermDto.TermRecommendation> rec2 = businessTermService.recommendTerms("사업자번호");
        assertThat(rec2).isNotEmpty();
        assertThat(rec2.get(0).getTerm().getTermCode()).isEqualTo("BIZ_NO");
        assertThat(rec2.get(0).getMatchReason()).contains("동의어");
    }
}
