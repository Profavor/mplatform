package com.favorsoft.mplatform.cdn.mapper;

import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface MasterMapper {
    List<Map<String, Object>> getMasterData(MasterDTO masterDTO);
    List<PropValue> getClassProp(MasterDTO masterDTO);
    List<Map<String, Object>> getChildClasses(MasterDTO masterDTO);
}
