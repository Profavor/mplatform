package com.classification.domain_system.repository;

import com.classification.domain_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    org.springframework.data.domain.Page<User> findByUsernameContainingIgnoreCase(String username, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<User> findByUsernameContainingIgnoreCaseOrRoleContainingIgnoreCase(String username, String role, org.springframework.data.domain.Pageable pageable);
    void deleteByOrganizationId(java.util.UUID organizationId);
    java.util.List<User> findByOrganizationId(java.util.UUID organizationId);
}
