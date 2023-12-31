package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Domain;
import com.favorsoft.mplatform.cdn.repository.jpa.DomainRepository;
import com.favorsoft.mplatform.cdn.service.DomainService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;

    public DomainServiceImpl(DomainRepository domainRepository){
        this.domainRepository = domainRepository;
    }

    @Override
    public List<Domain> getList() {
        return domainRepository.findAll();
    }

    @Override
    public Domain save(Domain domain) {
        return domainRepository.saveAndFlush(domain);
    }

    @Override
    public Domain getObject(Object key) {
        return domainRepository.findById(String.valueOf(key)).orElse(new Domain());
    }

    @Override
    public void delete(Object key) {
        domainRepository.deleteById(String.valueOf(key));
    }
}
