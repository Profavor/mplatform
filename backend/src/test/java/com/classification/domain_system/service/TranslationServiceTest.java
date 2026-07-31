package com.classification.domain_system.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @InjectMocks
    private TranslationService translationService;

    @Test
    @DisplayName("한국어 문장 입력 시 영어로 자동 번역 수행 검증")
    void translateText_KoreanToEnglish() {
        // given
        String koreanText = "안녕하세요 오늘 날씨가 참 좋네요";

        // when
        String translated = translationService.translateText(koreanText, "en");

        // then
        assertThat(translated).isNotNull();
        assertThat(translated.toLowerCase()).contains("hello");
    }

    @Test
    @DisplayName("영어 문장 입력 시 한국어로 자동 번역 수행 검증")
    void translateText_EnglishToKorean() {
        // given
        String englishText = "Thank you very much for your help";

        // when
        String translated = translationService.translateText(englishText, "ko");

        // then
        assertThat(translated).isNotNull();
        assertThat(translated).contains("감사");
    }
}
