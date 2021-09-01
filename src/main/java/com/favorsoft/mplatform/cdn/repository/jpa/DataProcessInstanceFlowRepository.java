package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstanceFlow;
import com.favorsoft.mplatform.cdn.domain.keys.DataProcessInstanceFlowKey;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface DataProcessInstanceFlowRepository extends JpaRepository<DataProcessInstanceFlow, DataProcessInstanceFlowKey> {
}
