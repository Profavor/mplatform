package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Mgroup;
import com.favorsoft.mplatform.cdn.repository.jpa.MgroupRepository;
import com.favorsoft.mplatform.cdn.service.MgroupService;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class MgroupServiceImpl implements MgroupService {

    private final MgroupRepository mgroupRepository;

    public MgroupServiceImpl(MgroupRepository mgroupRepository){
        this.mgroupRepository = mgroupRepository;
    }

    @Override
    public List<Mgroup> getList() {
        return mgroupRepository.findAll();
    }

    @Override
    public Mgroup save(Mgroup mgroup) {
        return mgroupRepository.saveAndFlush(mgroup);
    }

    @Override
    public Mgroup getObject(Object key) {
        String groupId = (String) key;
        return mgroupRepository.findById(groupId).orElse(new Mgroup());
    }

    @Override
    public void delete(Object key) {
        mgroupRepository.deleteById(String.valueOf(key));
    }
}
