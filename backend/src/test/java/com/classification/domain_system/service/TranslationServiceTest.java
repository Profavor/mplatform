package com.classification.domain_system.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TranslationService translationService;

    @Test
    @DisplayName("한국어 문장 입력 시 영어로 자동 번역 수행 검증")
    void translateText_KoreanToEnglish() {
        // given
        String koreanText = "안녕하세요 오늘 날씨가 참 좋네요";
        String mockResponse = "[[[\"Hello, the weather is very nice today.\",\"안녕하세요 오늘 날씨가 참 좋네요\",null,null,1]],null,\"ko\"]";
        given(restTemplate.getForObject(any(URI.class), eq(String.class))).willReturn(mockResponse);

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
        String mockResponse = "[[[\"도움을 주셔서 대단히 감사합니다.\",\"Thank you very much for your help\",null,null,1]],null,\"en\"]";
        given(restTemplate.getForObject(any(URI.class), eq(String.class))).willReturn(mockResponse);

        // when
        String translated = translationService.translateText(englishText, "ko");

        // then
        assertThat(translated).isNotNull();
        assertThat(translated).contains("감사");
    }

    @Test
    @DisplayName("외부 번역 API 호출 실패 시 원본 텍스트를 안전하게 폴백 반환한다")
    void translateText_FallbackOnApiFailure() {
        // given
        String text = "시스템 오류 발생 시 원본 유지";
        given(restTemplate.getForObject(any(URI.class), eq(String.class)))
                .willThrow(new RestClientException("Connection timed out or 429 Too Many Requests"));

        // when
        String translated = translationService.translateText(text, "en");

        // then
        assertThat(translated).isEqualTo(text);
    }

    @Test
    @DisplayName("빈 문자열 또는 null 입력 시 빈 문자열을 반환한다")
    void translateText_EmptyOrNull() {
        assertThat(translationService.translateText(null, "en")).isEmpty();
        assertThat(translationService.translateText("   ", "en")).isEmpty();
    }
}
