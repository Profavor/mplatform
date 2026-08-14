package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MasterRelationRequest;
import com.classification.domain_system.entity.MasterRelation;
import com.classification.domain_system.service.MasterRelationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterRelationControllerTest {

    @Mock
    private MasterRelationService masterRelationService;

    @InjectMocks
    private MasterRelationController masterRelationController;

    private MasterRelation createMockRelation() {
        MasterRelation relation = new MasterRelation();
        relation.setId(UUID.randomUUID());
        relation.setSourceDomainId(UUID.randomUUID());
        relation.setSourceFieldKey("companyId");
        relation.setTargetDomainId(UUID.randomUUID());
        relation.setRelationType("ONE_TO_MANY");
        relation.setCascadePolicy("RESTRICT");
        relation.setIsActive(true);
        return relation;
    }

    @Test
    @DisplayName("마스터 관계 생성 성공")
    void testCreateRelation_Success() {
        MasterRelationRequest request = new MasterRelationRequest();
        MasterRelation relation = createMockRelation();

        when(masterRelationService.createRelation(any(MasterRelationRequest.class))).thenReturn(relation);

        ResponseEntity<MasterRelation> response = masterRelationController.createRelation(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("companyId", response.getBody().getSourceFieldKey());
        verify(masterRelationService).createRelation(any(MasterRelationRequest.class));
    }

    @Test
    @DisplayName("전체 마스터 관계 목록 조회 성공")
    void testGetAllRelations_Success() {
        MasterRelation relation = createMockRelation();

        when(masterRelationService.getAllRelations()).thenReturn(List.of(relation));

        ResponseEntity<List<MasterRelation>> response = masterRelationController.getAllRelations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("companyId", response.getBody().get(0).getSourceFieldKey());
    }

    @Test
    @DisplayName("마스터 관계 수정 성공")
    void testUpdateRelation_Success() {
        UUID id = UUID.randomUUID();
        MasterRelationRequest request = new MasterRelationRequest();
        MasterRelation relation = createMockRelation();
        relation.setId(id);

        when(masterRelationService.updateRelation(eq(id), any(MasterRelationRequest.class))).thenReturn(relation);

        ResponseEntity<MasterRelation> response = masterRelationController.updateRelation(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().getId());
        verify(masterRelationService).updateRelation(eq(id), any(MasterRelationRequest.class));
    }

    @Test
    @DisplayName("마스터 관계 삭제 성공")
    void testDeleteRelation_Success() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = masterRelationController.deleteRelation(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(masterRelationService).deleteRelation(id);
    }
}
