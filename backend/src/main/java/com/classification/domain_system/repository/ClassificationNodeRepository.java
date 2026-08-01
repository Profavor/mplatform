package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ClassificationNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassificationNodeRepository extends JpaRepository<ClassificationNode, UUID> {
    List<ClassificationNode> findByDomain_IdAndAxisIsNullAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(UUID domainId);
    List<ClassificationNode> findByDomain_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(UUID domainId);
    List<ClassificationNode> findByDomain_IdAndAxis_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(UUID domainId, UUID axisId);
    List<ClassificationNode> findByParentIdAndIsDeletedFalseOrderByOrderAsc(UUID parentId);
    List<ClassificationNode> findByPathStartingWithAndIsDeletedFalse(String pathPrefix);
    List<ClassificationNode> findByDomain_Id(UUID domainId);
}
