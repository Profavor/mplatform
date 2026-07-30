package com.classification.domain_system.service;

import com.classification.domain_system.dto.MasterRelationRequest;
import com.classification.domain_system.entity.MasterRelation;
import com.classification.domain_system.repository.MasterRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterRelationServiceTest {

    @Mock
    private MasterRelationRepository masterRelationRepository;

    @InjectMocks
    private MasterRelationService masterRelationService;

    @Test
    void testCreateRelation() {
        MasterRelationRequest request = new MasterRelationRequest();
        request.setSourceDomainId(UUID.randomUUID());
        request.setTargetDomainId(UUID.randomUUID());
        request.setRelationType("ONE_TO_MANY");
        request.setCascadePolicy("RESTRICT");
        request.setSourceFieldKey("target_id");
        
        when(masterRelationRepository.save(any(MasterRelation.class))).thenAnswer(i -> i.getArgument(0));

        MasterRelation result = masterRelationService.createRelation(request);
        assertEquals("RESTRICT", result.getCascadePolicy());
        assertTrue(result.getIsActive());
    }

    @Test
    void testCheckReferentialIntegrity() {
        UUID targetDomainId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();

        MasterRelation relation = new MasterRelation();
        relation.setCascadePolicy("RESTRICT");
        relation.setIsActive(true);

        when(masterRelationRepository.findByTargetDomainId(targetDomainId))
                .thenReturn(Arrays.asList(relation));

        boolean canDelete = masterRelationService.checkReferentialIntegrity(recordId, targetDomainId);
        assertFalse(canDelete, "Should return false when RESTRICT policy is active");
    }
}
