package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.CodeGroup;
import com.favorsoft.mplatform.cdn.domain.CodeGroupProp;
import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.request.CodeGroupSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.*;
import com.favorsoft.mplatform.cdn.dto.result.CodeGroupSystemResult;
import com.favorsoft.mplatform.cdn.enums.PropMode;
import com.favorsoft.mplatform.cdn.mapper.CodeGroupMapper;
import com.favorsoft.mplatform.cdn.repository.jpa.CodeGroupRepository;
import com.favorsoft.mplatform.cdn.service.CodeGroupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackOn = Exception.class)
public class CodeGroupServiceImpl implements CodeGroupService {

    private static Logger logger = LoggerFactory.getLogger(CodeGroupServiceImpl.class);

    private final CodeGroupRepository codeGroupRepository;

    private final CodeGroupMapper codeGroupMapper;

    public CodeGroupServiceImpl(CodeGroupRepository codeGroupRepository, CodeGroupMapper codeGroupMapper){
        this.codeGroupRepository = codeGroupRepository;
        this.codeGroupMapper = codeGroupMapper;
    }

    @Override
    public List<CodeGroup> getList() {
        return codeGroupRepository.findAll();
    }

    @Override
    public CodeGroup save(CodeGroup codeGroup) {
        codeGroupMapper.createTable(codeGroup.getCodeGroupId());

        List<CodeGroupProp> codeGroupPropList = codeGroup.getCodeGroupProps();
        if(codeGroupPropList != null){
            for(CodeGroupProp codeGroupProp: codeGroupPropList){
                Map<String, Object> columnInfo = codeGroupMapper.getColumn(codeGroupProp.getCodeGroup().getCodeGroupId(),codeGroupProp.getProp().getPropId());
                if(columnInfo.isEmpty()){
                    codeGroupMapper.addColumn(codeGroupProp.getCodeGroup().getCodeGroupId(), codeGroupProp.getProp().getPropId(), codeGroupProp.getProp().getDbType());
                }
            }
        }

        return codeGroupRepository.saveAndFlush(codeGroup);
    }

    @Override
    public CodeGroup getObject(Object key) {
        return codeGroupRepository.findById(String.valueOf(key)).orElse(new CodeGroup());
    }

    @Override
    public void delete(Object key) {
        String codeGroupId = (String) key;
        codeGroupRepository.deleteById(codeGroupId);
        codeGroupMapper.dropTable(codeGroupId);
    }

    @Override
    public List<Map<String, Object>> getCodeList(String codeGroupId) {
        CodeGroup codeGroup = getObject(codeGroupId);
        List<CodeGroupProp> codeGroupProps = codeGroup.getCodeGroupProps();

        CodeGroupProp nameProp = codeGroupProps.stream().filter(s-> PropMode.NAME.equals(s.getPropMode())).findFirst().orElse(new CodeGroupProp());

        List<Map<String, Object>> codeMapList = codeGroupMapper.getCodeList(codeGroupId);
        for(Map<String, Object> codeMap: codeMapList){
            codeMap.put("NAME", codeMap.get(nameProp.getProp().getPropId()));
        }

        return codeMapList;
    }

    @Override
    public Map<String, Object> getCode(String codeGroupId, String code) {
        CodeGroup codeGroup = getObject(codeGroupId);
        List<CodeGroupProp> codeGroupProps = codeGroup.getCodeGroupProps();

        CodeGroupProp nameProp = codeGroupProps.stream().filter(s-> PropMode.NAME.equals(s.getPropMode())).findFirst().orElse(new CodeGroupProp());

        Map<String, Object> codeMap =  codeGroupMapper.getCode(codeGroupId, code);

        codeMap.put("NAME", codeMap.get(nameProp.getProp().getPropId()));

        return codeMap;
    }

    @Override
    public void saveCode(CodeGroupDTO codeGroupDTO) {
        CodeGroup codeGroup = getObject(codeGroupDTO.getCodeGroupId());
        List<CodeGroupProp> codeGroupProps = codeGroup.getCodeGroupProps();

        CodeGroupProp idProp = codeGroupProps.stream().filter(s-> PropMode.IDENTITY.equals(s.getPropMode())).findFirst().orElse(new CodeGroupProp());

        for(PropValue pv: codeGroupDTO.getData()){
            if(idProp.getProp() != null && pv.getPropId().equals(idProp.getProp().getPropId())){
                codeGroupDTO.setCode(pv.getValue());
            }
            if("Y".equals(codeGroupProps.stream().filter(s-> s.getProp().getPropId().equals(pv.getPropId())).findFirst().get().getIsUnique())){
                //중복 검사

            }
        }
        codeGroupMapper.saveCode(codeGroupDTO);

    }

    @Override
    public void deleteCode(String codeGroupId, String code) {
        codeGroupMapper.deleteCode(codeGroupId, code);
    }

    @Override
    public CodeGroupSystemRes getFormatedFormUI(CodeGroupSystemReq codeGroupSystemReq) {
        List<CodeGroupSystemResult> resultList =
                codeGroupMapper.getFormatedFormUI(codeGroupSystemReq.getCodeGroupId(),
                        codeGroupSystemReq.getLang().getLanguage());

        resultList.sort((r1, r2) -> {
            int sectionCompare = r1.getSectionDispSeq().compareToIgnoreCase(r2.getSectionDispSeq());

            if (sectionCompare == 0) {
                int groupCompare = r1.getGroupDispSeq().compareToIgnoreCase(r2.getGroupDispSeq());

                if(groupCompare == 0) {
                    return  r1.getGroupDispSeq().compareToIgnoreCase(r2.getDispSeq());
                }else{
                    return groupCompare;
                }

            } else {
                return sectionCompare;
            }
        });

        return getStandardSystemRes(resultList, codeGroupSystemReq.getCodeGroupId());
    }

    private CodeGroupSystemRes getStandardSystemRes(List<CodeGroupSystemResult> codeGroupSystemResultList,
                                                  String codeGroupId) {
        CodeGroupSystemRes codeGroupSystem = CodeGroupSystemRes.builder()
                .codeGroup(CodeGroupRes.builder().codeGroupId(codeGroupId).build())
                .msections(new ArrayList<>())
                .build();

        MsectionRes currentSection = null;
        MgroupRes currentGroup = null;
        for (CodeGroupSystemResult codeGroupSystemResult : codeGroupSystemResultList) {
            if (currentSection == null || !currentSection.getSectionId().equals(codeGroupSystemResult.getSectionId())) {
                currentSection = MsectionRes.builder()
                        .sectionId(codeGroupSystemResult.getSectionId())
                        .dispSeq(codeGroupSystemResult.getSectionDispSeq())
                        .isEnable(codeGroupSystemResult.getSectionIsEnable())
                        .message(codeGroupSystemResult.getSectionMessage())
                        .mgroups(new ArrayList<>())
                        .build();
                codeGroupSystem.addMsection(currentSection);
                currentGroup = null;
            }

            if (currentGroup == null || !currentGroup.getGroupId().equals(codeGroupSystemResult.getGroupId())) {
                currentGroup = MgroupRes.builder()
                        .groupId(codeGroupSystemResult.getGroupId())
                        .dispSeq(codeGroupSystemResult.getGroupDispSeq())
                        .isEnable(codeGroupSystemResult.getGroupIsEnable())
                        .message(codeGroupSystemResult.getGroupMessage())
                        .props(new ArrayList<>())
                        .build();
                currentSection.addMgroup(currentGroup);
            }

            OptionsRes options = OptionsRes.builder()
                    .propMode(codeGroupSystemResult.getPropMode())
                    .isDisabled(codeGroupSystemResult.getIsDisabled())
                    .isReadOnly(codeGroupSystemResult.getIsReadOnly())
                    .isShow(codeGroupSystemResult.getIsShow())
                    .dispSeq(codeGroupSystemResult.getDispSeq())
                    .build();

            PropRes prop = PropRes.builder()
                    .propId(codeGroupSystemResult.getPropId())
                    .areaId(codeGroupSystemResult.getAreaId())
                    .type(codeGroupSystemResult.getType())
                    .unit(codeGroupSystemResult.getUnit())
                    .width(codeGroupSystemResult.getWidth())
                    .regex(codeGroupSystemResult.getRegex())
                    .ruleCode(codeGroupSystemResult.getRuleCode())
                    .mask(codeGroupSystemResult.getMask())
                    .dbType(codeGroupSystemResult.getDbType())
                    .message(codeGroupSystemResult.getPropMessage())
                    .propMode(codeGroupSystemResult.getPropMode())
                    .options(options)
                    .build();

            currentGroup.addProp(prop);
        }
        if(codeGroupSystem.getMsections().size() > 0){
            if(codeGroupSystem.getMsections().get(0).getSectionId() == null){
                codeGroupSystem.setMsections(null);
            }
        }
        return codeGroupSystem;
    }
}
