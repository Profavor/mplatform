package com.classification.domain_system.service.storage;

import com.classification.domain_system.exception.BusinessException;
import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private LocalStorageService localStorageService;

    @Mock
    private MultipartFile multipartFile;

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        FileValidationUtil validationUtil = new FileValidationUtil("jpg,jpeg,png,gif,pdf,txt,xlsx,xls,csv,docx,doc,pptx,ppt,txt,zip");
        storageService = new MinioStorageService(
                "http://localhost:9000", "test-access-key", "test-secret-key", "test-bucket", validationUtil, localStorageService);
        ReflectionTestUtils.setField(storageService, "minioClient", minioClient);
    }

    @Test
    void storeFile_Success_ReturnsHashedFilename() throws Exception {
        // Given
        byte[] content = "test file content".getBytes();
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // When
        String savedFilename = storageService.storeFile(multipartFile);

        // Then
        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".pdf"), "Should preserve file extension");
        assertEquals(64 + 4, savedFilename.length(), "SHA-256 hash (64 chars) + .pdf (4 chars)");
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void storeFile_InvalidPathSequence_ThrowsException() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("../malicious.txt");

        // When / Then
        assertThrows(BusinessException.class, () -> storageService.storeFile(multipartFile));
        verifyNoInteractions(minioClient);
    }

    @Test
    void storeFile_RestrictedExtension_ThrowsException() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("shell.html");

        // When / Then
        assertThrows(BusinessException.class, () -> storageService.storeFile(multipartFile));
        verifyNoInteractions(minioClient);
    }

    @Test
    void storeFile_DisallowedExtension_ThrowsException() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("script.py");

        // When / Then
        assertThrows(BusinessException.class, () -> storageService.storeFile(multipartFile));
        verifyNoInteractions(minioClient);
    }

    @Test
    void loadFileAsResource_Success_ReturnsResource() throws Exception {
        // Given
        String filename = "sample.pdf";
        byte[] expectedContent = "PDF file content".getBytes();
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);
        when(getObjectResponse.readAllBytes()).thenReturn(expectedContent);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        // When
        Resource resource = storageService.loadFileAsResource(filename);

        // Then
        assertNotNull(resource);
        assertEquals(filename, resource.getFilename());
        try (InputStream is = resource.getInputStream()) {
            assertArrayEquals(expectedContent, is.readAllBytes());
        }
    }

    @Test
    void loadFileAsResource_InvalidPath_ThrowsException() {
        assertThrows(BusinessException.class, () -> storageService.loadFileAsResource("../bad.pdf"));
        verifyNoInteractions(minioClient);
    }

    @Test
    void deleteFile_Success_CallsRemoveObject() throws Exception {
        // Given
        String filename = "file-to-delete.pdf";
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // When
        storageService.deleteFile(filename);

        // Then
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFile_InvalidPath_ThrowsException() {
        assertThrows(BusinessException.class, () -> storageService.deleteFile("../bad.pdf"));
        verifyNoInteractions(minioClient);
    }
}
