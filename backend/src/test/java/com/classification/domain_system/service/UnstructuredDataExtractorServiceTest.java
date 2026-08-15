package com.classification.domain_system.service;

import com.classification.domain_system.dto.UnstructuredDataDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UnstructuredDataExtractorServiceTest {

    private UnstructuredDataExtractorService extractorService;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        extractorService = new UnstructuredDataExtractorService();
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("extractFields: 비정형 텍스트로부터 정형 마스터 데이터 필드 자동 추출")
    void testExtractFields() {
        String rawText = """
                [용역 표준 계약서 요약]
                발주사: 주식회사 글로벌엔터프라이즈 (사업자등록번호: 123-45-67890)
                계약 금액: 50,000,000 원 (VAT 별도)
                담당자 연락처: 010-1234-5678, 이메일: contact@enterprise.com
                """;

        UnstructuredDataDto.ExtractionResponse res = extractorService.extractFields(domainId, rawText);

        assertThat(res).isNotNull();
        assertThat(res.getOverallConfidence()).isGreaterThan(0.8);
        assertThat(res.getSuggestedRecordCode()).startsWith("REC-AI-");
        assertThat(res.getFields()).hasSizeGreaterThanOrEqualTo(4);

        boolean hasBizNo = res.getFields().stream().anyMatch(f -> "biz_reg_no".equals(f.getFieldKey()) && "123-45-67890".equals(f.getExtractedValue()));
        assertThat(hasBizNo).isTrue();
    }

    @Test
    @DisplayName("extractFields: 빈 텍스트 입력 시 안전한 기본 응답 반환")
    void testExtractFieldsEmpty() {
        UnstructuredDataDto.ExtractionResponse res = extractorService.extractFields(domainId, "");

        assertThat(res).isNotNull();
        assertThat(res.getFields()).isEmpty();
        assertThat(res.getOverallConfidence()).isEqualTo(0.0);
    }
}
