package com.classification.domain_system.controller;

import com.classification.domain_system.entity.TaxonomyVersion;
import com.classification.domain_system.service.TaxonomyVersionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyVersionControllerTest {

    @Mock
    private TaxonomyVersionService taxonomyVersionService;

    @InjectMocks
    private TaxonomyVersionController taxonomyVersionController;

    private TaxonomyVersion createMockVersion(UUID domainId, String label) {
        TaxonomyVersion version = new TaxonomyVersion();
        version.setId(UUID.randomUUID());
        version.setDomainId(domainId);
        version.setVersionLabel(label);
        version.setSnapshotData("{\"nodes\":[]}");
        version.setPublishedBy("admin");
        version.setPublishedAt(LocalDateTime.now());
        version.setIsActive(true);
        return version;
    }

    @Test
    @DisplayName("분류 체계 스냅샷 생성 성공")
    void testCreateSnapshot_Success() {
        UUID domainId = UUID.randomUUID();
        Map<String, String> request = Map.of("label", "v1.0", "publishedBy", "admin");
        TaxonomyVersion version = createMockVersion(domainId, "v1.0");

        when(taxonomyVersionService.createSnapshot(domainId, "v1.0", "admin")).thenReturn(version);

        ResponseEntity<TaxonomyVersion> response = taxonomyVersionController.createSnapshot(domainId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("v1.0", response.getBody().getVersionLabel());
        verify(taxonomyVersionService).createSnapshot(domainId, "v1.0", "admin");
    }

    @Test
    @DisplayName("도메인별 분류 체계 버전 목록 조회 성공")
    void testGetVersions_Success() {
        UUID domainId = UUID.randomUUID();
        TaxonomyVersion version = createMockVersion(domainId, "v1.0");

        when(taxonomyVersionService.getVersions(domainId)).thenReturn(List.of(version));

        ResponseEntity<List<TaxonomyVersion>> response = taxonomyVersionController.getVersions(domainId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("v1.0", response.getBody().get(0).getVersionLabel());
    }

    @Test
    @DisplayName("분류 체계 스냅샷 원본 데이터 조회 성공")
    void testGetSnapshotData_Success() {
        UUID domainId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        String json = "{\"nodes\":[{\"name\":\"root\"}]}";

        when(taxonomyVersionService.getSnapshotData(versionId)).thenReturn(json);

        ResponseEntity<String> response = taxonomyVersionController.getSnapshotData(domainId, versionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(json, response.getBody());
    }
}
