package com.classification.domain_system.repository;

import com.classification.domain_system.entity.CodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeDetailRepository extends JpaRepository<CodeDetail, UUID> {
    List<CodeDetail> findByCodeGroupIdOrderBySortOrderAsc(UUID groupId);
    List<CodeDetail> findByCodeGroupIdAndIsActiveTrue(UUID groupId);
    List<CodeDetail> findByCodeGroupGroupCode(String groupCode);
}
