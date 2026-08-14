package com.classification.domain_system.controller;

import com.classification.domain_system.service.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileController fileController;

    @Test
    @DisplayName("파일 업로드 성공")
    void testUploadFile_Success() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.png", "image/png", "dummy-image-content".getBytes()
        );

        when(fileStorageService.storeFile(any())).thenReturn("stored-test.png");

        ResponseEntity<Map<String, String>> response = fileController.uploadFile(multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test.png", response.getBody().get("fileName"));
        assertTrue(response.getBody().get("url").contains("stored-test.png"));
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    void testDownloadFile_Success() {
        byte[] content = "dummy file content".getBytes();
        Resource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };

        when(fileStorageService.loadFileAsResource("test.txt")).thenReturn(resource);

        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<?> response = fileController.downloadFile("test.txt", "test.txt", headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 404 반환")
    void testDownloadFile_NotFound() {
        when(fileStorageService.loadFileAsResource("nonexistent.txt")).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<?> response = fileController.downloadFile("nonexistent.txt", null, headers);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
