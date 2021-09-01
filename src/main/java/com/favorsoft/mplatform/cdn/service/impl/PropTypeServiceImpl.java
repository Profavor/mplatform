package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.PropType;
import com.favorsoft.mplatform.cdn.repository.jpa.PropTypeRepository;
import com.favorsoft.mplatform.cdn.service.PropTypeService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class PropTypeServiceImpl implements PropTypeService {

    private final PropTypeRepository propTypeRepository;

    public PropTypeServiceImpl(final PropTypeRepository propTypeRepository) {
        this.propTypeRepository = propTypeRepository;
    }

    @Override
    public List<PropType> getList() {
        return propTypeRepository.findAll();
    }

    @Override
    public PropType save(PropType propType) {
        return propTypeRepository.saveAndFlush(propType);
    }

    @Override
    public PropType getObject(Object key) {
        return propTypeRepository.findById(String.valueOf(key)).orElse(new PropType());
    }

    @Override
    public void delete(Object key) {
        propTypeRepository.deleteById(String.valueOf(key));
    }
}
