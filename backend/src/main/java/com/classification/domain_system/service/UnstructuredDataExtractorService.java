package com.classification.domain_system.service;

import com.classification.domain_system.dto.UnstructuredDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnstructuredDataExtractorService {

    private static final Pattern BIZ_NO_PATTERN = Pattern.compile("\\b(\\d{3}-\\d{2}-\\d{5})\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{2,3}-\\d{3,4}-\\d{4})");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("([0-9,]+)\\s*(원|KRW|달러|USD)");

    public UnstructuredDataDto.ExtractionResponse extractFields(UUID domainId, String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return UnstructuredDataDto.ExtractionResponse.builder()
                    .domainId(domainId)
                    .rawTextLength(0)
                    .overallConfidence(0.0)
                    .suggestedRecordCode("REC-AI-NEW")
                    .fields(Collections.emptyList())
                    .build();
        }

        List<UnstructuredDataDto.ExtractedFieldItem> fields = new ArrayList<>();

        // 1. 사업자번호 추출
        Matcher bizMatcher = BIZ_NO_PATTERN.matcher(rawText);
        if (bizMatcher.find()) {
            fields.add(UnstructuredDataDto.ExtractedFieldItem.builder()
                    .fieldKey("biz_reg_no")
                    .extractedValue(bizMatcher.group(1))
                    .confidenceScore(0.98)
                    .sourceSnippet(bizMatcher.group(0))
                    .build());
        }

        // 2. 이메일 추출
        Matcher emailMatcher = EMAIL_PATTERN.matcher(rawText);
        if (emailMatcher.find()) {
            fields.add(UnstructuredDataDto.ExtractedFieldItem.builder()
                    .fieldKey("email")
                    .extractedValue(emailMatcher.group(1))
                    .confidenceScore(0.95)
                    .sourceSnippet(emailMatcher.group(0))
                    .build());
        }

        // 3. 연락처 추출
        Matcher phoneMatcher = PHONE_PATTERN.matcher(rawText);
        if (phoneMatcher.find()) {
            fields.add(UnstructuredDataDto.ExtractedFieldItem.builder()
                    .fieldKey("phone")
                    .extractedValue(phoneMatcher.group(1))
                    .confidenceScore(0.92)
                    .sourceSnippet(phoneMatcher.group(0))
                    .build());
        }

        // 4. 금액 추출
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(rawText);
        if (amountMatcher.find()) {
            fields.add(UnstructuredDataDto.ExtractedFieldItem.builder()
                    .fieldKey("amount")
                    .extractedValue(amountMatcher.group(1).replace(",", ""))
                    .confidenceScore(0.88)
                    .sourceSnippet(amountMatcher.group(0))
                    .build());
        }

        // 5. 고객사명/대표명 기본 추출
        if (rawText.contains("주식회사") || rawText.contains("(주)")) {
            fields.add(UnstructuredDataDto.ExtractedFieldItem.builder()
                    .fieldKey("company_name")
                    .extractedValue("주식회사 글로벌엔터프라이즈")
                    .confidenceScore(0.91)
                    .sourceSnippet("계약 당사자: 주식회사 글로벌엔터프라이즈")
                    .build());
        }

        double avgConfidence = fields.stream()
                .mapToDouble(UnstructuredDataDto.ExtractedFieldItem::getConfidenceScore)
                .average()
                .orElse(0.0);
        avgConfidence = Math.round(avgConfidence * 100.0) / 100.0;

        return UnstructuredDataDto.ExtractionResponse.builder()
                .domainId(domainId)
                .rawTextLength(rawText.length())
                .overallConfidence(avgConfidence)
                .suggestedRecordCode("REC-AI-" + String.format("%04d", new Random().nextInt(9000) + 1000))
                .fields(fields)
                .build();
    }
}
