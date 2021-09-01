package com.favorsoft.mplatform.cdn.repository.jpa;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.favorsoft.mplatform.cdn.domain.ErrorHistory;

@CrossOrigin
@Repository
@JaversSpringDataAuditable
public interface ErrorHistoryRepository extends JpaRepository<ErrorHistory, String> {

}
