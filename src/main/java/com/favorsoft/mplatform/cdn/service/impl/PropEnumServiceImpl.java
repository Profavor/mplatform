package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.PropEnum;
import com.favorsoft.mplatform.cdn.domain.keys.PropEnumKey;
import com.favorsoft.mplatform.cdn.repository.jpa.PropEnumRepository;
import com.favorsoft.mplatform.cdn.service.PropEnumService;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class PropEnumServiceImpl implements PropEnumService {

    private final PropEnumRepository propEnumRepository;

    public PropEnumServiceImpl(PropEnumRepository propEnumRepository){
        this.propEnumRepository = propEnumRepository;
    }

    @Override
    public List<PropEnum> getList() {
        return propEnumRepository.findAll();
    }

    @Override
    public PropEnum save(PropEnum entity) {
        return propEnumRepository.save(entity);
    }

    @Override
    public PropEnum getObject(Object key) {
        PropEnumKey tempKey = (PropEnumKey) key;
        return propEnumRepository.findById(tempKey).orElse(new PropEnum());
    }

    @Override
    public void delete(Object key) {
        PropEnumKey tempKey = (PropEnumKey) key;
        propEnumRepository.deleteById(tempKey);
    }

    @Override
    public List<PropEnum> getListByPropId(String propId) {
        return propEnumRepository.findByPropId(propId);
    }

    @Override
    public PropEnum getPropEnum(String propId, String code) {
        return propEnumRepository.findByPropIdAndCode(propId, code).orElse(new PropEnum());
    }
}
