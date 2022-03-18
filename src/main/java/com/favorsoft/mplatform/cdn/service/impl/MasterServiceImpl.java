package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Master;
import com.favorsoft.mplatform.cdn.domain.Prop;
import com.favorsoft.mplatform.cdn.domain.keys.MasterKey;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.PropValueHistory;
import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.mapper.MasterMapper;
import com.favorsoft.mplatform.cdn.mapper.PropMapper;
import com.favorsoft.mplatform.cdn.repository.mongo.MasterMongoCustomRepository;
import com.favorsoft.mplatform.cdn.repository.jpa.MasterRepository;
import com.favorsoft.mplatform.cdn.service.MasterCodeService;
import com.favorsoft.mplatform.cdn.service.MasterService;
import com.favorsoft.mplatform.cdn.service.PropService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackOn = Exception.class)
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;
    private final MasterMongoCustomRepository masterMongoCustomRepository;
    private final PropService propService;
    private final MasterMapper masterMapper;
    private final PropMapper propMapper;
    private final MasterCodeService masterCodeService;

    public MasterServiceImpl(MasterRepository masterRepository, MasterMongoCustomRepository masterMongoCustomRepository,
                             MasterMapper masterMapper, PropService propService, PropMapper propMapper,
                             MasterCodeService masterCodeService){
        this.masterRepository = masterRepository;
        this.masterMongoCustomRepository = masterMongoCustomRepository;
        this.masterMapper = masterMapper;
        this.propMapper = propMapper;
        this.propService = propService;
        this.masterCodeService = masterCodeService;
    }

    @Override
    public List<Map<String, Object>> getMasterData(MasterDTO masterDTO) {
        List<PropValue> propList = masterMapper.getClassProp(masterDTO);
        masterDTO.setProps(propList.stream().filter(s->s.getPropId() != null).map(PropValue::getPropId).collect(Collectors.toList()));
        masterDTO.setClasses(masterMapper.getChildClasses(masterDTO));

        return masterMapper.getMasterData(masterDTO);
    }

    @Override
    public void saveMaster(MasterDTO masterDTO) {
        Master master = masterRepository.findById(new MasterKey(masterDTO.getDomainId(), masterDTO.getMasterId()))
                .orElse(new Master(masterDTO.getDomainId(),
                        masterDTO.getMasterId() == null ?  masterCodeService.createMasterId(masterDTO.getDomainId(), masterDTO.getClassId()) : masterDTO.getMasterId(), masterDTO.getClassId()));

        BeanUtils.copyProperties(masterDTO, master, CommonUtil.getNullPropertyNames(masterDTO));

        masterRepository.save(master);

        masterDTO.getData().forEach(p -> {
            PropValue propValue = PropValue.convert(masterDTO, p);
            propValue.setMasterId(master.getMasterId());
            savePvAndPvHistory(propValue);
        });

        saveMasterToMongo(masterDTO);
    }

    @Override
    public void deleteMaster(MasterDTO masterDTO) {
        MasterKey masterKey = new MasterKey(masterDTO.getDomainId(), masterDTO.getMasterId());
        this.delete(masterKey);

        List<Prop> propList = propService.getList();
        propList.forEach(p -> {
            PropValue propValue = PropValue.convert(masterDTO, p);
            deletePropValue(propValue);
            deletePropValueHistory(propValue);
        });
    }

    @Override
    public void savePvAndPvHistory(PropValue propValue) {
        savePropValue(propValue);
        savePropValueHistory(propValue);
    }

    @Override
    public void deletePvAndPvHistory(PropValue propValue) {
        savePropValue(propValue);
        savePropValueHistory(propValue);
    }

    private void savePropValue(PropValue propValue) {
        propMapper.insertProp(propValue);
    }

    private void deletePropValue(PropValue propValue) {
        propMapper.deleteProp(propValue);
    }

    private void deletePropValueHistory(PropValue propValue) {
        propMapper.deletePropHistory(propValue);
    }

    private void savePropValueHistory(PropValue propValue) {
        List<PropValueHistory> historyList = propMapper.selectAllPropHistory(propValue.getPropId());
        Optional<PropValueHistory> maxPropValueHistory = historyList.stream().filter(s-> s.getMasterId().equals(propValue.getMasterId())).max(Comparator.comparingInt(h -> Integer.parseInt(h.getSeq())));
        String maxSeq = maxPropValueHistory
                .map(valueHistory -> String.valueOf(Integer.parseInt(valueHistory.getSeq()) + 1))
                .orElse("1");

        PropValueHistory propValueHistory = PropValueHistory.convert(propValue, maxSeq);
        propMapper.insertPropHistory(propValueHistory);
    }

    private void saveMasterToMongo(MasterDTO masterDTO) {
        if(masterMongoCustomRepository.collectionExists(masterDTO.getDomainId())) {
            MasterDTO temp = masterMongoCustomRepository.masterIdExists(masterDTO);
            if(temp != null){
                masterMongoCustomRepository.update(masterDTO);
            }else{
                masterMongoCustomRepository.insert(masterDTO);
            }
        } else {
            masterMongoCustomRepository.create(masterDTO.getDomainId());
            masterMongoCustomRepository.insert(masterDTO);
        }
    }

    @Override
    public List<Master> getList() {
        return masterRepository.findAll();
    }

    @Override
    public Master save(Master entity) {
        return masterRepository.save(entity);
    }

    @Override
    public Master getObject(Object key) {
        MasterKey masterKey = (MasterKey) key;
        return masterRepository.findById(masterKey).orElse(new Master());
    }

    @Override
    public void delete(Object key) {
        masterRepository.delete(getObject(key));
    }
}
