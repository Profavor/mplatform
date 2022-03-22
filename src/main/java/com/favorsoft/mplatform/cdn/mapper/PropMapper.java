package com.favorsoft.mplatform.cdn.mapper;

import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.PropValueHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PropMapper {

    int insertProp(PropValue propValue);

    int insertPropHistory(PropValueHistory propValueHistory);

    int createPropTable(@Param("propId") String propId, @Param("dbType") String dbType);

    int createPropHistoryTable(@Param("propId") String propId,@Param("dbType") String dbType);

    int dropPropTable(@Param("propId") String propId);

    int dropPropHistoryTable(@Param("propId") String propId);

    List<PropValue> selectAllProp(@Param("propId") String propId);

    List<PropValueHistory> selectAllPropHistory(@Param("propId") String propId);

    int deleteProp(PropValue propValue);

    int deletePropHistory(PropValue propValue);

}
