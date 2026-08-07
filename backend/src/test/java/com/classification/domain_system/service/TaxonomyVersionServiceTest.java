package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.TaxonomyVersion;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.TaxonomyVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyVersionServiceTest {

    @Mock
    private TaxonomyVersionRepository taxonomyVersionRepository;
    
    @Mock
    private ClassificationNodeRepository classificationNodeRepository;

    @InjectMocks
    private TaxonomyVersionService taxonomyVersionService;

    @Test
    void testCreateSnapshot() throws Exception {
        UUID domainId = UUID.randomUUID();
        when(taxonomyVersionRepository.save(any(TaxonomyVersion.class))).thenAnswer(i -> i.getArgument(0));

        ClassificationNode rootNode = new ClassificationNode();
        rootNode.setId(UUID.randomUUID());
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("ko", "Root Node");
        rootNode.setName(nameMap);
        rootNode.setDepth(0);
        rootNode.setOrder(1);
        
        ClassificationNode childNode = new ClassificationNode();
        childNode.setId(UUID.randomUUID());
        childNode.setParent(rootNode);
        childNode.setDepth(1);
        childNode.setIsDeleted(false);
        rootNode.getChildren().add(childNode);

        when(classificationNodeRepository.findByDomain_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId))
                .thenReturn(List.of(rootNode));
                
        TaxonomyVersion result = taxonomyVersionService.createSnapshot(domainId, "v1.0", "admin");
        
        assertEquals("v1.0", result.getVersionLabel());
        assertNotNull(result.getSnapshotData());
        assertTrue(result.getSnapshotData().contains("Root Node"));
        
        verify(classificationNodeRepository, times(1)).findByDomain_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId);
    }
}
