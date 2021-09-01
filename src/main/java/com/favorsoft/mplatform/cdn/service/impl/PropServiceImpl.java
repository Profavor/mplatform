package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Prop;
import com.favorsoft.mplatform.cdn.mapper.PropMapper;
import com.favorsoft.mplatform.cdn.repository.jpa.PropRepository;
import com.favorsoft.mplatform.cdn.service.PropService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class PropServiceImpl implements PropService {
    private final PropRepository propRepository;
    private final PropMapper propMapper;

    public PropServiceImpl(PropRepository propRepository, PropMapper propMapper){
        this.propRepository = propRepository;
        this.propMapper = propMapper;
    }

    @Override
    public List<Prop> getList() {
        return propRepository.findAll();
    }

    @Override
    public Prop save(Prop prop) {
        propMapper.createPropTable(prop.getPropId(), prop.getDbType());
        propMapper.createPropHistoryTable(prop.getPropId(), prop.getDbType());
        return propRepository.save(prop);
    }

    @Override
    public Prop getObject(Object key) {
        return propRepository.findById(String.valueOf(key)).orElse(new Prop());
    }

    @Override
    public void delete(Object key) {
        propMapper.dropPropTable(String.valueOf(key));         //Drop Prop
        propMapper.dropPropHistoryTable(String.valueOf(key));  //Drop Prop History
        propRepository.deleteById(String.valueOf(key));
    }
}
