package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.Mclass;
import com.favorsoft.mplatform.cdn.domain.keys.MclassKey;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface MclassRepository extends JpaRepository<Mclass, MclassKey> {
    List<Mclass> findByDomainId(String domainId);
}
