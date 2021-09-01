package com.favorsoft.mplatform.cdn.repository.jpa;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.favorsoft.mplatform.cdn.domain.MasterCode;
import com.favorsoft.mplatform.cdn.domain.keys.MasterCodeKey;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface MasterCodeRepository extends JpaRepository<MasterCode, MasterCodeKey> {
    <T> T findByClassIdAndDomainId(String classId, String domainId, Class<T> type);
}
