package com.classification.domain_system.service;

import com.classification.domain_system.entity.TaxonomyVersion;
import com.classification.domain_system.repository.TaxonomyVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyVersionServiceTest {

    @Mock
    private TaxonomyVersionRepository taxonomyVersionRepository;

    @InjectMocks
    private TaxonomyVersionService taxonomyVersionService;

    @Test
    void testCreateSnapshot() {
        UUID domainId = UUID.randomUUID();
        when(taxonomyVersionRepository.save(any(TaxonomyVersion.class))).thenAnswer(i -> i.getArgument(0));

        TaxonomyVersion result = taxonomyVersionService.createSnapshot(domainId, "v1.0", "admin");
        assertEquals("v1.0", result.getVersionLabel());
        assertEquals("[]", result.getSnapshotData());
    }
}
