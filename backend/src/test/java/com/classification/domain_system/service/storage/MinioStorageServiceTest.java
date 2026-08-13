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
    private MultipartFile multipartFile;

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        // Create service via constructor, then replace the internal minioClient with our mock
        storageService = new MinioStorageService(
                "http://localhost:9000", "test-access-key", "test-secret-key", "test-bucket", "./uploads");
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
    void storeFile_PathTraversal_ThrowsException() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("../../../etc/passwd");

        // When & Then
        assertThrows(BusinessException.class, () -> storageService.storeFile(multipartFile));
    }

    @Test
    void storeFile_CreatesBucketIfNotExists() throws Exception {
        // Given
        byte[] content = "data".getBytes();
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // When
        storageService.storeFile(multipartFile);

        // Then
        verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void loadFileAsResource_Success_ReturnsResource() throws Exception {
        // Given
        byte[] content = "file content bytes".getBytes();
        InputStream mockStream = new ByteArrayInputStream(content);
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.readAllBytes()).thenReturn(content);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // When
        Resource resource = storageService.loadFileAsResource("abc123.pdf");

        // Then
        assertNotNull(resource);
        assertEquals("abc123.pdf", resource.getFilename());
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void loadFileAsResource_PathTraversal_ThrowsException() {
        // When & Then
        assertThrows(BusinessException.class, () ->
                storageService.loadFileAsResource("../../secret.txt"));
    }

    @Test
    void deleteFile_Success_CallsRemoveObject() throws Exception {
        // When
        storageService.deleteFile("abc123.pdf");

        // Then
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFile_PathTraversal_ThrowsException() {
        // When & Then
        assertThrows(BusinessException.class, () ->
                storageService.deleteFile("../../../etc/passwd"));
    }
}
