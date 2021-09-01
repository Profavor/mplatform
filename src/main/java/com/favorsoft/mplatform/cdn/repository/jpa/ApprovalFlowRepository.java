package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface ApprovalFlowRepository extends JpaRepository<ApprovalFlow, Long> {

    List<ApprovalFlow> findByUserAndApprovalActivityCode(User user, ApprovalActivityCode approvalActivityCode);
}
