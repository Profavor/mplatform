package com.favorsoft.mplatform.cdn.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.favorsoft.mplatform.cdn.domain.PropEnum;
import com.favorsoft.mplatform.cdn.domain.keys.PropEnumKey;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface PropEnumRepository  extends JpaRepository<PropEnum, PropEnumKey> {
    List<PropEnum> findByPropId(String propId);

    Optional<PropEnum> findByPropIdAndCode(String propId, String code);
}
