package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Domain;
import com.favorsoft.mplatform.cdn.domain.Mclass;
import com.favorsoft.mplatform.cdn.dto.request.StandardSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.*;
import com.favorsoft.mplatform.cdn.dto.result.StandardSystemResult;
import com.favorsoft.mplatform.cdn.mapper.MclassMapper;
import com.favorsoft.mplatform.cdn.repository.jpa.MclassRepository;
import com.favorsoft.mplatform.cdn.service.MclassService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(rollbackOn = Exception.class)
public class MclassServiceImpl implements MclassService {

    private final MclassRepository mclassRepository;
    private final MclassMapper mclassMapper;

    public MclassServiceImpl(MclassRepository mclassRepository, MclassMapper mclassMapper){
        this.mclassRepository = mclassRepository;
        this.mclassMapper = mclassMapper;
    }

    @Override
    public List<Mclass> getList(){
        return mclassRepository.findAll();
    }

    @Override
    public List<Mclass> getList(String domainId) {
        return mclassRepository.findByDomainOrderByDispSeqAsc(new Domain(domainId));
    }

    @Override
    public Mclass save(Mclass mclass){
        String classId = mclass.getClassId();
        String parentId = mclass.getParentId();

        if(classId.equals(parentId)){
            mclass.setParentId(null);
        }
        return mclassRepository.saveAndFlush(mclass);
    }

    @Override
    public Mclass getObject(Object key) {
        return mclassRepository.findById((String)key).orElse(new Mclass());
    }

    @Override
    public void delete(Object key){ 
        Mclass mclass = getObject(key);
        mclassRepository.delete(mclass);
    }

    @Override
    public StandardSystemRes getStandardSystemByClassAndDomain(StandardSystemReq standardSystemReq) {
        List<StandardSystemResult> resultList =
                mclassMapper.getFormatedFormUI(standardSystemReq.getDomainId(),
                                                                    standardSystemReq.getClassId(),
                                                                    standardSystemReq.getLang().getLanguage());

        List<StandardSystemResult> distinctList = distinctResult(resultList);

        distinctList.sort((r1, r2) -> {
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

        return getStandardSystemRes(distinctList, standardSystemReq.getClassId(), standardSystemReq.getDomainId());
    }

    private List<StandardSystemResult> distinctResult(List<StandardSystemResult> resultList) {
        List<StandardSystemResult> distinctList = new ArrayList<>();
        Map<String, StandardSystemResult> seen = new ConcurrentHashMap<>();

        for (int i = 0; i < resultList.size(); i++) {
            StandardSystemResult standardSystem = resultList.get(i);
            if(StringUtils.hasText(standardSystem.getPropId())) {
                StandardSystemResult putStandardSystem = seen.putIfAbsent(standardSystem.getPropId(), standardSystem);
                if (putStandardSystem == null) {
                    distinctList.add(standardSystem);
                } else {
                    if (putStandardSystem.getLevel() < standardSystem.getLevel()) {
                        distinctList.remove(putStandardSystem);
                        distinctList.add(standardSystem);
                        seen.put(standardSystem.getPropId(), standardSystem);
                    }
                }
            }
        }

        return distinctList;
    }

    public StandardSystemRes getStandardSystemRes(List<StandardSystemResult> standardSystemResultList,
                                                  String classId, String domainId) {
        StandardSystemRes standardSystem = StandardSystemRes.builder()
                .domain(DomainRes.builder().domainId(domainId).build())
                .mclass(
                        MclassRes.builder()
                                .classId(classId)
                                .message(!standardSystemResultList.isEmpty()?standardSystemResultList.get(0).getClassMessage() : null)
                                .build())
                .msections(new ArrayList<>())
                .build();

        MsectionRes currentSection = null;
        MgroupRes currentGroup = null;
        for (StandardSystemResult standardSystemResult : standardSystemResultList) {
            if (currentSection == null || !currentSection.getSectionId().equals(standardSystemResult.getSectionId())) {
                currentSection = MsectionRes.builder()
                        .sectionId(standardSystemResult.getSectionId())
                        .dispSeq(standardSystemResult.getSectionDispSeq())
                        .isEnable(standardSystemResult.getSectionIsEnable())
                        .message(standardSystemResult.getSectionMessage())
                        .mgroups(new ArrayList<>())
                        .build();
                standardSystem.addMsection(currentSection);
                currentGroup = null;
            }

            if (currentGroup == null || !currentGroup.getGroupId().equals(standardSystemResult.getGroupId())) {
                currentGroup = MgroupRes.builder()
                        .groupId(standardSystemResult.getGroupId())
                        .dispSeq(standardSystemResult.getGroupDispSeq())
                        .isEnable(standardSystemResult.getGroupIsEnable())
                        .message(standardSystemResult.getGroupMessage())
                        .props(new ArrayList<>())
                        .build();
                currentSection.addMgroup(currentGroup);
            }

            OptionsRes options = OptionsRes.builder()
                    .isDisabled(standardSystemResult.getIsDisabled())
                    .isReadOnly(standardSystemResult.getIsReadOnly())
                    .isShow(standardSystemResult.getIsShow())
                    .dispSeq(standardSystemResult.getDispSeq())
                    .propMode(standardSystemResult.getPropMode())
                    .build();

            PropRes prop = PropRes.builder()
                    .propId(standardSystemResult.getPropId())
                    .areaId(standardSystemResult.getAreaId())
                    .type(standardSystemResult.getType())
                    .unit(standardSystemResult.getUnit())
                    .width(standardSystemResult.getWidth())
                    .regex(standardSystemResult.getRegex())
                    .ruleCode(standardSystemResult.getRuleCode())
                    .mask(standardSystemResult.getMask())
                    .dbType(standardSystemResult.getDbType())
                    .message(standardSystemResult.getPropMessage())
                    .options(options)
                    .reference(standardSystemResult.getReference())
                    .build();

            currentGroup.addProp(prop);
        }
        return standardSystem;
    }

    @Override
    public List<Mclass> getList(String domainId, String isEnable) {
        return mclassRepository.findByDomainAndIsEnableOrderByDispSeqAsc(new Domain(domainId), isEnable);
    }
}
