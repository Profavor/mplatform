package com.favorsoft.mplatform.cdn.service.dataprocess.impl;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstanceFlow;
import com.favorsoft.mplatform.cdn.domain.keys.DataProcessInstanceFlowKey;
import com.favorsoft.mplatform.cdn.repository.jpa.DataProcessInstanceFlowRepository;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessInstanceFlowService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DataProcessInstanceFlowServiceImpl implements DataProcessInstanceFlowService {

    private final DataProcessInstanceFlowRepository dataProcessInstanceFlowRepository;

    public DataProcessInstanceFlowServiceImpl(DataProcessInstanceFlowRepository dataProcessInstanceFlowRepository){
        this.dataProcessInstanceFlowRepository = dataProcessInstanceFlowRepository;
    }

    @Override
    public List<DataProcessInstanceFlow> getList() {
        return dataProcessInstanceFlowRepository.findAll();
    }

    @Override
    public DataProcessInstanceFlow save(DataProcessInstanceFlow entity) {
        return dataProcessInstanceFlowRepository.saveAndFlush(entity);
    }

    @Override
    public DataProcessInstanceFlow getObject(Object key) {
        DataProcessInstanceFlowKey approvalInstanceFlowKey = (DataProcessInstanceFlowKey) key;
        return dataProcessInstanceFlowRepository.findById(approvalInstanceFlowKey).orElse(new DataProcessInstanceFlow());
    }

    @Override
    public void delete(Object key) {
        dataProcessInstanceFlowRepository.delete(this.getObject(key));
    }
}
