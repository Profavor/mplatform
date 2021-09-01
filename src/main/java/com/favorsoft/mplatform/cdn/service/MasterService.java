package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.Master;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.List;
import java.util.Map;

public interface MasterService extends CommonService<Master> {

    void saveMaster(MasterDTO masterDTO);

    void deleteMaster(MasterDTO masterDTO);

    void savePvAndPvHistory(PropValue propValue);

    void deletePvAndPvHistory(PropValue propValue);

    List<Map<String, Object>> getMasterData(MasterDTO masterDTO);
}
