package com.classification.domain_system.controller;

import com.classification.domain_system.service.TranslationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/translate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> translateMessage(@RequestBody TranslationRequest req) {
        String original = req.getText();
        String translated = translationService.translateText(original, req.getTargetLang());
        return ResponseEntity.ok(Map.of(
                "original", original != null ? original : "",
                "translated", translated != null ? translated : ""
        ));
    }

    @Data
    public static class TranslationRequest {
        private String text;
        private String targetLang;
    }
}
