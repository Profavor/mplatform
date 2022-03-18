package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.Domain;
import com.favorsoft.mplatform.cdn.domain.Mclass;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface MclassRepository extends JpaRepository<Mclass, String> {
    List<Mclass> findByDomainOrderByDispSeqAsc(Domain domain);

    List<Mclass> findByDomainAndIsEnableOrderByDispSeqAsc(Domain domain, String isEnable);
}
