package com.favorsoft.mplatform.cdn.service.dataprocess;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstance;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.List;

public interface DataProcessInstanceService extends CommonService<DataProcessInstance> {
    List<DataProcessInstance> getInstanceList(ApprovalStatus status);
}
