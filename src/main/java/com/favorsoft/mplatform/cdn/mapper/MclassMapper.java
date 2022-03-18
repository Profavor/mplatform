package com.favorsoft.mplatform.cdn.mapper;

import com.favorsoft.mplatform.cdn.dto.result.StandardSystemResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MclassMapper {
    List<StandardSystemResult> getFormatedFormUI(@Param("domainId") String domainId, @Param("classId") String classId, @Param("lang") String lang);
}
