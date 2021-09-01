package com.favorsoft.mplatform.cdn.service.dataprocess;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstance;
import com.favorsoft.mplatform.cdn.domain.DataProcessInstanceFlow;

public interface DataProcessManager {
    void startDataProcess(DataProcessInstance dataProcessInstance);

    void endDataProcess(DataProcessInstance dataProcessInstance);

    void changeStep(DataProcessInstanceFlow currentFlow, DataProcessInstanceFlow nextFlow);

    void acceptDataProcess(DataProcessInstance dataProcessInstance);

    void rejectDataProcess(DataProcessInstance dataProcessInstance);
}
