package com.classification.domain_system.repository;

import com.classification.domain_system.entity.UserYoutubeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserYoutubeConfigRepository extends JpaRepository<UserYoutubeConfig, UUID> {
    Optional<UserYoutubeConfig> findByUserId(String userId);
}
