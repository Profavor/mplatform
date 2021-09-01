package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.CodeGroup;
import com.favorsoft.mplatform.cdn.domain.CodeGroupProp;
import com.favorsoft.mplatform.cdn.domain.Prop;
import com.favorsoft.mplatform.cdn.domain.keys.CodeGroupPropKey;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface CodeGroupPropRepository extends JpaRepository<CodeGroupProp, Long> {
    CodeGroupProp findByCodeGroupAndProp(CodeGroup codeGroup, Prop prop);
}
