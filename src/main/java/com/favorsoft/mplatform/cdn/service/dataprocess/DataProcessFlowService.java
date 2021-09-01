package com.favorsoft.mplatform.cdn.service.dataprocess;

import com.favorsoft.mplatform.cdn.domain.DataProcessFlow;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.List;

public interface DataProcessFlowService extends CommonService<DataProcessFlow> {
    List<DataProcessFlow> getList(String processId);
}
