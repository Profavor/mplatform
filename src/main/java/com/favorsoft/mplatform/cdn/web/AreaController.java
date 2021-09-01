package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Area;
import com.favorsoft.mplatform.cdn.service.AreaService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/areas")
public class AreaController {
    private final AreaService areaService;

    public AreaController(AreaService areaService){
        this.areaService = areaService;
    }

    @GetMapping
    public List<Area> getList() {
        return areaService.getList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Area> save(@RequestBody Area area) {
        Area originArea = areaService.getObject(area.getAreaId());
        BeanUtils.copyProperties(area, originArea, CommonUtil.getNullPropertyNames(area));

        return ResponseEntity.ok(areaService.save(area));
    }

    @GetMapping(value = "/{areaId}")
    public Area getObject(@PathVariable String areaId){
        return areaService.getObject(areaId);
    }

    @DeleteMapping(value = "/{areaId}")
    public ResponseEntity<Void> delete(@PathVariable String areaId) {
        areaService.delete(areaId);
        return ResponseEntity.ok().build();
    }
}
