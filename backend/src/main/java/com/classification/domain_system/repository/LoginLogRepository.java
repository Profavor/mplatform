package com.classification.domain_system.repository;

import com.classification.domain_system.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    boolean existsByUsernameAndLoginAtAfter(String username, LocalDateTime after);
}
