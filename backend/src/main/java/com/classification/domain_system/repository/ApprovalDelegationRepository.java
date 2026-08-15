package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ApprovalDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalDelegationRepository extends JpaRepository<ApprovalDelegation, UUID> {

    List<ApprovalDelegation> findByDelegatorUserIdOrderByCreatedAtDesc(String delegatorUserId);

    List<ApprovalDelegation> findByDelegateeUserIdOrderByCreatedAtDesc(String delegateeUserId);

    @Query("SELECT d FROM ApprovalDelegation d WHERE d.delegateeUserId = :delegateeUserId AND d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<ApprovalDelegation> findActiveDelegationsForDelegatee(@Param("delegateeUserId") String delegateeUserId, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM ApprovalDelegation d WHERE d.delegatorUserId = :delegatorUserId AND d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<ApprovalDelegation> findActiveDelegationsForDelegator(@Param("delegatorUserId") String delegatorUserId, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM ApprovalDelegation d WHERE d.delegatorUserId = :delegatorUserId AND d.delegateeUserId = :delegateeUserId AND d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<ApprovalDelegation> findActiveDelegationPair(@Param("delegatorUserId") String delegatorUserId, @Param("delegateeUserId") String delegateeUserId, @Param("now") LocalDateTime now);
}
