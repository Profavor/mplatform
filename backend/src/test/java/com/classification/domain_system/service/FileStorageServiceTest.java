package com.classification.domain_system.service;

import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.service.storage.FileStorageService;
import com.classification.domain_system.service.storage.FileValidationUtil;
import com.classification.domain_system.service.storage.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        FileValidationUtil validationUtil = new FileValidationUtil("jpg,jpeg,png,gif,pdf,txt,xlsx,xls,csv,docx,doc,pptx,ppt,zip");
        fileStorageService = new LocalStorageService(tempDir.toString(), validationUtil);
    }

    @Test
    void testStoreFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );

        String storedFileName = fileStorageService.storeFile(file);

        assertNotNull(storedFileName);
        assertTrue(storedFileName.contains("test.txt") || storedFileName.length() > 0);
        
        Resource resource = fileStorageService.loadFileAsResource(storedFileName);
        assertTrue(resource.exists());
    }

    @Test
    void testStoreFile_InvalidName() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../invalid_path.txt",
                "text/plain",
                "Invalid Content".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    void testLoadFileAsResource_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Sample Content".getBytes(StandardCharsets.UTF_8)
        );

        String storedFileName = fileStorageService.storeFile(file);
        Resource resource = fileStorageService.loadFileAsResource(storedFileName);

        assertNotNull(resource);
        assertTrue(resource.exists());
        try (InputStream is = resource.getInputStream()) {
            assertEquals("Sample Content", new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testLoadFileAsResource_NotFound() {
        assertThrows(RuntimeException.class, () -> fileStorageService.loadFileAsResource("non_existent_file.txt"));
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to_delete.txt",
                "text/plain",
                "Content to delete".getBytes(StandardCharsets.UTF_8)
        );

        String storedFileName = fileStorageService.storeFile(file);
        Resource resourceBefore = fileStorageService.loadFileAsResource(storedFileName);
        assertTrue(resourceBefore.exists());

        fileStorageService.deleteFile(storedFileName);

        assertThrows(RuntimeException.class, () -> fileStorageService.loadFileAsResource(storedFileName));
    }
}
