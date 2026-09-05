package com.classification.domain_system.repository;

import com.classification.domain_system.entity.MenuAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuAccessLogRepository extends JpaRepository<MenuAccessLog, Long>, JpaSpecificationExecutor<MenuAccessLog> {
    java.util.List<MenuAccessLog> findTop20ByUserIdOrderByAccessedAtDesc(String userId);
}

