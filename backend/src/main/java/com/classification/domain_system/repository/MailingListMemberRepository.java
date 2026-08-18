package com.classification.domain_system.repository;

import com.classification.domain_system.entity.MailingListMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MailingListMemberRepository extends JpaRepository<MailingListMember, UUID> {
    List<MailingListMember> findByMailingListId(UUID mailingListId);
    List<MailingListMember> findByUserId(String userId);
    void deleteByMailingListIdAndUserId(UUID mailingListId, String userId);
}
