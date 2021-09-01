package com.favorsoft.mplatform.manager.dataprocess;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstance;
import com.favorsoft.mplatform.cdn.domain.DataProcessInstanceFlow;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessManager;
import org.springframework.stereotype.Component;

@Component
public class BasicDataProcessManager implements DataProcessManager {
    @Override
    public void startDataProcess(DataProcessInstance dataProcessInstance) {

    }

    @Override
    public void endDataProcess(DataProcessInstance dataProcessInstance) {

    }

    @Override
    public void changeStep(DataProcessInstanceFlow currentFlow, DataProcessInstanceFlow nextFlow) {

    }

    @Override
    public void acceptDataProcess(DataProcessInstance dataProcessInstance) {

    }

    @Override
    public void rejectDataProcess(DataProcessInstance dataProcessInstance) {

    }
}
