package com.classification.domain_system.service;

import com.classification.domain_system.dto.MasterRelationRequest;
import com.classification.domain_system.entity.MasterRelation;
import com.classification.domain_system.repository.MasterRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MasterRelationService {

    private final MasterRelationRepository masterRelationRepository;
    private final com.classification.domain_system.repository.RecordRepository recordRepository;

    @Transactional
    public MasterRelation createRelation(MasterRelationRequest request) {
        MasterRelation relation = new MasterRelation();
        relation.setSourceDomainId(request.getSourceDomainId());
        relation.setSourceFieldKey(request.getSourceFieldKey());
        relation.setTargetDomainId(request.getTargetDomainId());
        relation.setRelationType(request.getRelationType());
        relation.setCascadePolicy(request.getCascadePolicy());
        relation.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return masterRelationRepository.save(relation);
    }

    @Transactional
    public MasterRelation updateRelation(UUID id, MasterRelationRequest request) {
        MasterRelation relation = masterRelationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MasterRelation not found"));
        if (request.getSourceDomainId() != null) relation.setSourceDomainId(request.getSourceDomainId());
        if (request.getSourceFieldKey() != null) relation.setSourceFieldKey(request.getSourceFieldKey());
        if (request.getTargetDomainId() != null) relation.setTargetDomainId(request.getTargetDomainId());
        if (request.getRelationType() != null) relation.setRelationType(request.getRelationType());
        if (request.getCascadePolicy() != null) relation.setCascadePolicy(request.getCascadePolicy());
        if (request.getIsActive() != null) relation.setIsActive(request.getIsActive());
        return masterRelationRepository.save(relation);
    }

    @Transactional
    public void deleteRelation(UUID id) {
        masterRelationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MasterRelation> getAllRelations() {
        return masterRelationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean checkReferentialIntegrity(UUID recordId, UUID targetDomainId) {
        List<MasterRelation> relations = masterRelationRepository.findByTargetDomainId(targetDomainId);
        
        for (MasterRelation relation : relations) {
            if ("RESTRICT".equalsIgnoreCase(relation.getCascadePolicy()) && Boolean.TRUE.equals(relation.getIsActive())) {
                boolean isReferenced = recordRepository.existsReferencingRecord(
                        relation.getSourceDomainId(), 
                        relation.getSourceFieldKey(),
                        recordId.toString());
                
                if (isReferenced) {
                    return false; // Safe to delete is false, because it's referenced
                }
            }
        }
        
        return true; 
    }
}
