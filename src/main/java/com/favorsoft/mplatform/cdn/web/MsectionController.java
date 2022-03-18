package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Msection;
import com.favorsoft.mplatform.cdn.service.MsectionService;

import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/sections")
public class MsectionController {
    private final MsectionService msectionService;

    public MsectionController(MsectionService msectionService){
        this.msectionService = msectionService;
    }

    @GetMapping
    public List<Msection> getList() {
        return msectionService.getList();
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Msection> save(@RequestBody Msection Msection) {
        Msection originMsection = msectionService.getObject(Msection.getSectionId());

        BeanUtils.copyProperties(Msection, originMsection, CommonUtil.getNullPropertyNames(Msection));

        return ResponseEntity.ok(msectionService.save(originMsection));
    }

    @GetMapping(value = "/{sectionId}")
    public Msection getObject(@PathVariable String sectionId){
        return msectionService.getObject(sectionId);
    }

    @DeleteMapping(value = "/{sectionId}")
    public ResponseEntity delete(@PathVariable String sectionId) {
        msectionService.delete(sectionId);
        return ResponseEntity.ok().build();
    }
}
