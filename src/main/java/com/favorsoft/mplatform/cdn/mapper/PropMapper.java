package com.favorsoft.mplatform.cdn.mapper;

import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.PropValueHistory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PropMapper {

    int insertProp(PropValue propValue);

    int insertPropHistory(PropValueHistory propValueHistory);

    int createPropTable(String propId, String dbType);

    int createPropHistoryTable(String propId, String dbType);

    int dropPropTable(String propId);

    int dropPropHistoryTable(String propId);

    List<PropValue> selectAllProp(String propId);

    List<PropValueHistory> selectAllPropHistory(String propId);

    int deleteProp(PropValue propValue);

    int deletePropHistory(PropValue propValue);

}
