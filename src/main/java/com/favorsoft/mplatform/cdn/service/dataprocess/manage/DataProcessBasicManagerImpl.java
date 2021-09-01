package com.favorsoft.mplatform.cdn.service.dataprocess.manage;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstance;
import com.favorsoft.mplatform.cdn.domain.DataProcessInstanceFlow;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataProcessBasicManagerImpl implements DataProcessManager {

    private static Logger logger = LoggerFactory.getLogger(DataProcessBasicManagerImpl.class);

    @Override
    public void startDataProcess(DataProcessInstance dataProcessInstance) {
        logger.info("##### startDataProcess #####");
    }

    @Override
    public void endDataProcess(DataProcessInstance dataProcessInstance) {
        logger.info("##### endDataProcess #####");
    }

    @Override
    public void changeStep(DataProcessInstanceFlow currentFlow, DataProcessInstanceFlow nextFlow) {
        logger.info("##### changeStep #####");
    }

    @Override
    public void acceptDataProcess(DataProcessInstance dataProcessInstance) {
        logger.info("##### acceptDataProcess #####");
    }

    @Override
    public void rejectDataProcess(DataProcessInstance dataProcessInstance) {
        logger.info("##### rejectDataProcess #####");
    }
}
