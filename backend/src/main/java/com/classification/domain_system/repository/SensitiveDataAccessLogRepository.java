package com.classification.domain_system.repository;

import com.classification.domain_system.entity.SensitiveDataAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SensitiveDataAccessLogRepository extends JpaRepository<SensitiveDataAccessLog, UUID> {

    Page<SensitiveDataAccessLog> findAllByOrderByAccessedAtDesc(Pageable pageable);

    Page<SensitiveDataAccessLog> findByUserIdOrderByAccessedAtDesc(String userId, Pageable pageable);

    Page<SensitiveDataAccessLog> findByTargetIdOrderByAccessedAtDesc(UUID targetId, Pageable pageable);

    List<SensitiveDataAccessLog> findByTargetIdAndTargetType(UUID targetId, String targetType);
}
