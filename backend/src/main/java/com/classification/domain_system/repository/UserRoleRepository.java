package com.classification.domain_system.repository;

import com.classification.domain_system.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUserId(String userId);
    List<UserRole> findByRoleId(UUID roleId);
    Optional<UserRole> findByUserIdAndRoleId(String userId, UUID roleId);
    void deleteByUserIdAndRoleId(String userId, UUID roleId);
    void deleteByRoleId(UUID roleId);
    void deleteByUserId(String userId);
}
