package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {
    
    List<Domain> findAllByOrderBySortOrderAsc();

    @Query("SELECT d FROM Domain d JOIN DomainPermission dp ON d.id = dp.domain.id WHERE dp.user.id = :userId ORDER BY d.sortOrder ASC")
    List<Domain> findAllByUserIdOrderBySortOrderAsc(@Param("userId") String userId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Domain d WHERE d.id = :id")
    java.util.Optional<Domain> findWithLockById(@Param("id") UUID id);

    java.util.Optional<Domain> findBySpecializedCategory(String specializedCategory);
}
