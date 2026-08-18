package com.classification.domain_system.repository;

import com.classification.domain_system.entity.MailingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MailingListRepository extends JpaRepository<MailingList, UUID> {
    List<MailingList> findByIsActiveTrue();
    Optional<MailingList> findByEmail(String email);
    boolean existsByEmail(String email);
}
