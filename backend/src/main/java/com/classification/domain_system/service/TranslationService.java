package com.classification.domain_system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String translateText(String text, String requestedTargetLang) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 언어 자동 감지 및 Target Language 설정 (기본: 한글 -> 영어, 영어 -> 한글)
        String targetLang = requestedTargetLang;
        if (targetLang == null || targetLang.trim().isEmpty()) {
            boolean isKorean = text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
            targetLang = isKorean ? "en" : "ko";
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://translate.googleapis.com/translate_a/single")
                    .queryParam("client", "gtx")
                    .queryParam("sl", "auto")
                    .queryParam("tl", targetLang)
                    .queryParam("dt", "t")
                    .queryParam("q", text)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            String response = restTemplate.getForObject(uri, String.class);
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
                    StringBuilder translatedSb = new StringBuilder();
                    for (JsonNode item : root.get(0)) {
                        if (item.isArray() && item.size() > 0) {
                            translatedSb.append(item.get(0).asText());
                        }
                    }
                    return translatedSb.toString();
                }
            }
        } catch (Exception e) {
            log.error("Failed to translate text via Google Translate API: {}", text, e);
        }

        return text;
    }
}
