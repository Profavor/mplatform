package com.classification.domain_system.repository;

import com.classification.domain_system.entity.UserOrgHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserOrgHistoryRepository extends JpaRepository<UserOrgHistory, UUID> {
    List<UserOrgHistory> findByUserIdOrderByChangedAtDesc(String userId);
    void deleteByUserId(String userId);
}
