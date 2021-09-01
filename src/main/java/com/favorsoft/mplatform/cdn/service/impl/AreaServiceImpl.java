package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Area;
import com.favorsoft.mplatform.cdn.repository.jpa.AreaRepository;
import com.favorsoft.mplatform.cdn.service.AreaService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;

    public AreaServiceImpl(final AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Override
    public List<Area> getList() {
        return areaRepository.findAll();
    }

    @Override
    public Area save(Area area) {
        return areaRepository.saveAndFlush(area);
    }

    @Override
    public Area getObject(Object key) {
        return areaRepository.findById(String.valueOf(key)).orElse(new Area());
    }

    @Override
    public void delete(Object key) {
        areaRepository.deleteById(String.valueOf(key));
    }
}
