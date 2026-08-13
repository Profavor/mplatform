package com.classification.domain_system.service.storage;

import com.classification.domain_system.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        FileValidationUtil validationUtil = new FileValidationUtil("jpg,png,pdf");
        File tempDir = Files.createTempDirectory("uploads").toFile();
        tempDir.deleteOnExit();
        storageService = new LocalStorageService(tempDir.getAbsolutePath(), validationUtil);
    }

    @Test
    void storeFile_Success_WithValidExtension() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes());
        String saved = storageService.storeFile(validFile);
        assertTrue(saved.endsWith(".jpg"));
    }

    @Test
    void storeFile_Failure_WithDangerousExtension() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.html", "text/html", "<html></html>".getBytes());
        assertThrows(BusinessException.class, () -> storageService.storeFile(invalidFile));
    }

    @Test
    void storeFile_Failure_WithNotAllowedExtension() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.mp3", "audio/mpeg", "fake-audio-content".getBytes());
        assertThrows(BusinessException.class, () -> storageService.storeFile(invalidFile));
    }
    
    @Test
    void storeFile_Failure_WithXssMagicBytes() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());
        assertThrows(BusinessException.class, () -> storageService.storeFile(invalidFile));
    }
}
