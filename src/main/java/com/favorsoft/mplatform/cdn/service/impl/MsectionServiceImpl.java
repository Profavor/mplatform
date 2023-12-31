package com.favorsoft.mplatform.cdn.service.impl;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.favorsoft.mplatform.cdn.domain.Msection;
import com.favorsoft.mplatform.cdn.repository.jpa.MsectionRepository;
import com.favorsoft.mplatform.cdn.service.MsectionService;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class MsectionServiceImpl implements MsectionService {

    private final MsectionRepository msectionRepository;

    public MsectionServiceImpl(MsectionRepository msectionRepository){
        this.msectionRepository = msectionRepository;
    }

    @Override
    public List<Msection> getList() {
        return msectionRepository.findAll();
    }

    @Override
    public Msection save(Msection Msection) {
        return msectionRepository.saveAndFlush(Msection);
    }

    @Override
    public Msection getObject(Object key) {
        String sectionId = (String) key;
        return msectionRepository.findById(sectionId).orElse(new Msection());
    }

    @Override
    public void delete(Object key) {
        msectionRepository.deleteById(String.valueOf(key));
    }
}
