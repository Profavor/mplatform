package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.ClassProp;
import com.favorsoft.mplatform.cdn.domain.keys.ClassPropKey;
import com.favorsoft.mplatform.cdn.enums.PropMode;
import com.favorsoft.mplatform.cdn.repository.jpa.ClassPropRepository;
import com.favorsoft.mplatform.cdn.service.ClassPropService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Transactional(rollbackOn = Exception.class)
@Service
public class ClassPropServiceImpl implements ClassPropService {

    private final ClassPropRepository classPropRepository;

    public ClassPropServiceImpl(ClassPropRepository classPropRepository){
        this.classPropRepository = classPropRepository;
    }

    @Override
    public List<ClassProp> getList() {
        return classPropRepository.findAll();
    }

    @Override
    public ClassProp save(ClassProp classProp) {
        return classPropRepository.saveAndFlush(classProp);
    }

    @Override
    public ClassProp getObject(Object key) {
        ClassPropKey classPropKey = (ClassPropKey) key;
        return classPropRepository.findById(classPropKey).orElse(new ClassProp());
    }

    @Override
    public void delete(Object key) {
        ClassPropKey classPropKey = (ClassPropKey) key;
        ClassProp classProp = getObject(classPropKey);
        classPropRepository.delete(classProp);
    }

    @Override
    public ClassProp getClassProp(String domainId, PropMode propMode) {
        return classPropRepository.findByDomainIdAndPropMode(domainId, propMode);
    }
}
