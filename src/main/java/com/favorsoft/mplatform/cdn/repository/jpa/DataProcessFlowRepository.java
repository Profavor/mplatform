package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.DataProcessFlow;
import com.favorsoft.mplatform.cdn.domain.keys.DataProcessFlowKey;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface DataProcessFlowRepository extends JpaRepository<DataProcessFlow, DataProcessFlowKey> {
    List<DataProcessFlow> findByProcessId(String processId);
}
