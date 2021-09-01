package com.favorsoft.mplatform.cdn.repository.jpa;

import com.favorsoft.mplatform.cdn.domain.Master;
import com.favorsoft.mplatform.cdn.domain.keys.MasterKey;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface MasterRepository extends JpaRepository<Master, MasterKey> {

}
