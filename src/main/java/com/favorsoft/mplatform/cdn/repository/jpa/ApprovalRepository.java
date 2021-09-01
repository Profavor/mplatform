package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByStatus(ApprovalStatus status);

    List<Approval> findByCurrentUserAndStatus(User user, ApprovalStatus status);
}
