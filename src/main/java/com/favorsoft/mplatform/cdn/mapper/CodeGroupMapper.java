package com.favorsoft.mplatform.cdn.mapper;

import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.result.CodeGroupSystemResult;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.List;

@Mapper
@Repository
public interface CodeGroupMapper {
    void createTable(String codeGroupId);

    void dropTable(String codeGroupId);

    void addColumn(String codeGroupId, String propId, String dbType);

    void dropColumn(String codeGroupId, String propId);

    List<Map<String, Object>> getCodeList(String codeGroupId);

    Map<String, Object> getCode(String codeGroupId, String code);

    Map<String, Object> getColumn(String codeGroupId, String field);

    void saveCode(CodeGroupDTO codeGroupDTO);

    void deleteCode(String codeGroupId, String code);

    List<CodeGroupSystemResult> getFormatedFormUI(String codeGroupId, String lang);

    int existCode(String codeGroupId, String code, String propId, String propValue);

}
