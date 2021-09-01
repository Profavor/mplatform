package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.CodeGroupProp;
import com.favorsoft.mplatform.cdn.domain.Prop;
import com.favorsoft.mplatform.cdn.domain.keys.CodeGroupPropKey;
import com.favorsoft.mplatform.cdn.mapper.CodeGroupMapper;
import com.favorsoft.mplatform.cdn.repository.jpa.CodeGroupPropRepository;
import com.favorsoft.mplatform.cdn.service.CodeGroupPropService;
import com.favorsoft.mplatform.cdn.service.PropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackOn = Exception.class)
public class CodeGroupPropServiceImpl implements CodeGroupPropService {

    private static Logger logger = LoggerFactory.getLogger(CodeGroupPropServiceImpl.class);

    private final CodeGroupPropRepository codeGroupPropRepository;

    private final CodeGroupMapper codeGroupMapper;

    private final PropService propService;

    public CodeGroupPropServiceImpl(CodeGroupPropRepository codeGroupPropRepository, CodeGroupMapper codeGroupMapper, PropService propService){
        this.codeGroupPropRepository = codeGroupPropRepository;
        this.codeGroupMapper = codeGroupMapper;
        this.propService = propService;
    }

    @Override
    public List<CodeGroupProp> getList() {
        return codeGroupPropRepository.findAll();
    }

    @Override
    public CodeGroupProp save(CodeGroupProp codeGroupProp) {
        Map<String, Object> columnInfo = codeGroupMapper.getColumn(codeGroupProp.getCodeGroup().getCodeGroupId(),codeGroupProp.getProp().getPropId());
        if(columnInfo == null){
            Prop prop = propService.getObject(codeGroupProp.getProp().getPropId());
            codeGroupMapper.addColumn(codeGroupProp.getCodeGroup().getCodeGroupId(), codeGroupProp.getProp().getPropId(), prop.getDbType());
        }
        return codeGroupPropRepository.saveAndFlush(codeGroupProp);
    }

    @Override
    public CodeGroupProp getObject(Object key) {
        return codeGroupPropRepository.findById((Long) key).orElse(new CodeGroupProp());
    }

    @Override
    public void delete(Object key) {
        CodeGroupProp codeGroupProp = getObject(key);
        if(codeGroupProp.getCodeGroup().getCodeGroupId() != null && codeGroupProp.getProp().getPropId() != null){
            Map<String, Object> columnInfo = codeGroupMapper.getColumn(codeGroupProp.getCodeGroup().getCodeGroupId(),codeGroupProp.getProp().getPropId());
            if(columnInfo != null) {
                codeGroupMapper.dropColumn(codeGroupProp.getCodeGroup().getCodeGroupId(), codeGroupProp.getProp().getPropId());
            }
            codeGroupPropRepository.delete(codeGroupProp);
        }
    }
}
