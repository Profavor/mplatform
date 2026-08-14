package com.classification.domain_system.controller;

import com.classification.domain_system.service.TranslationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationControllerTest {

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private TranslationController translationController;

    @Test
    @DisplayName("메시지 실시간 다국어 번역 성공")
    void testTranslateMessage_Success() {
        TranslationController.TranslationRequest request = new TranslationController.TranslationRequest();
        request.setText("안녕하세요");
        request.setTargetLang("en");

        when(translationService.translateText("안녕하세요", "en")).thenReturn("Hello");

        ResponseEntity<Map<String, String>> response = translationController.translateMessage(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("안녕하세요", response.getBody().get("original"));
        assertEquals("Hello", response.getBody().get("translated"));
        verify(translationService).translateText("안녕하세요", "en");
    }
}
