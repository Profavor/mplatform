package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.ClassProp;
import com.favorsoft.mplatform.cdn.domain.keys.ClassPropKey;
import com.favorsoft.mplatform.cdn.enums.PropMode;

import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface ClassPropRepository extends JpaRepository<ClassProp, ClassPropKey> {
    ClassProp findByDomainIdAndPropMode(String domainId, PropMode propMode);
    List<ClassProp> findByDomainIdAndClassId(String domainId, String classId);

}
