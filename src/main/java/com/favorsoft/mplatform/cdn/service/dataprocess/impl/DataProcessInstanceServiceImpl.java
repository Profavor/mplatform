package com.favorsoft.mplatform.cdn.service.dataprocess.impl;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstance;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.repository.jpa.DataProcessInstanceRepository;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessInstanceService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DataProcessInstanceServiceImpl implements DataProcessInstanceService {

    private final DataProcessInstanceRepository dataProcessInstanceRepository;

    public DataProcessInstanceServiceImpl(DataProcessInstanceRepository dataProcessInstanceRepository){
        this.dataProcessInstanceRepository = dataProcessInstanceRepository;
    }

    @Override
    public List<DataProcessInstance> getList() {
        return dataProcessInstanceRepository.findAll();
    }

    @Override
    public DataProcessInstance save(DataProcessInstance entity) {
        return dataProcessInstanceRepository.saveAndFlush(entity);
    }

    @Override
    public DataProcessInstance getObject(Object key) {
        return dataProcessInstanceRepository.findById(String.valueOf(key)).orElse(new DataProcessInstance());
    }

    @Override
    public void delete(Object key) {
        dataProcessInstanceRepository.delete(getObject(key));
    }

    @Override
    public List<DataProcessInstance> getInstanceList(ApprovalStatus status) {
        return dataProcessInstanceRepository.findByStatus(status);
    }
}
