package com.favorsoft.mplatform.cdn.service.dataprocess.impl;

import com.favorsoft.mplatform.cdn.domain.DataProcessFlow;
import com.favorsoft.mplatform.cdn.domain.keys.DataProcessFlowKey;
import com.favorsoft.mplatform.cdn.repository.jpa.DataProcessFlowRepository;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessFlowService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DataProcessFlowServiceImpl implements DataProcessFlowService {

    private final DataProcessFlowRepository dataProcessFlowRepository;

    public DataProcessFlowServiceImpl(DataProcessFlowRepository dataProcessFlowRepository) {
        this.dataProcessFlowRepository = dataProcessFlowRepository;
    }

    @Override
    public List<DataProcessFlow> getList() {
        return dataProcessFlowRepository.findAll();
    }

    @Override
    public DataProcessFlow save(DataProcessFlow entity) {
        return dataProcessFlowRepository.saveAndFlush(entity);
    }

    @Override
    public DataProcessFlow getObject(Object key) {
        return dataProcessFlowRepository.findById((DataProcessFlowKey) key).orElse(new DataProcessFlow());
    }

    @Override
    public void delete(Object key) {
        dataProcessFlowRepository.delete(getObject(key));
    }

    @Override
    public List<DataProcessFlow> getList(String processId) {
        return dataProcessFlowRepository.findByProcessId(processId);
    }
}
