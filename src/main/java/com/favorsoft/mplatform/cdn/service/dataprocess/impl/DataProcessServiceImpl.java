package com.favorsoft.mplatform.cdn.service.dataprocess.impl;

import com.favorsoft.mplatform.cdn.domain.DataProcess;
import com.favorsoft.mplatform.cdn.repository.jpa.DataProcessRepository;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DataProcessServiceImpl implements DataProcessService {

    private final DataProcessRepository dataProcessRepository;

    public DataProcessServiceImpl(DataProcessRepository dataProcessRepository){
        this.dataProcessRepository = dataProcessRepository;
    }

    @Override
    public List<DataProcess> getList() {
        return dataProcessRepository.findAll();
    }

    @Override
    public DataProcess save(DataProcess entity) {
        return dataProcessRepository.saveAndFlush(entity);
    }

    @Override
    public DataProcess getObject(Object key) {
        return dataProcessRepository.findById(String.valueOf(key)).orElse(new DataProcess());
    }

    @Override
    public void delete(Object key) {
        dataProcessRepository.delete(getObject(key));

    }
}
