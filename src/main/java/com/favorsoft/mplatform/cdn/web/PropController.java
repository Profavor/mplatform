package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Prop;
import com.favorsoft.mplatform.cdn.dto.PropDTO2;
import com.favorsoft.mplatform.cdn.dto.ResponseDTO;
import com.favorsoft.mplatform.cdn.service.PropService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/props")
public class PropController {
    private final PropService propService;

    public PropController(PropService propService){
        this.propService = propService;
    }

    @GetMapping
    public List<Prop> getList() {
        return propService.getList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Prop> save(@RequestBody Prop prop) {
        Prop originProp = propService.getObject(prop.getPropId());

        BeanUtils.copyProperties(prop, originProp, CommonUtil.getNullPropertyNames(prop));

        return ResponseEntity.ok(propService.save(originProp));
    }

    @GetMapping(value = "/{propId}")
    public Prop getObject(@PathVariable String propId){
        return propService.getObject(propId);
    }

    @DeleteMapping(value = "/{propId}")
    public ResponseEntity delete(@PathVariable String propId) {
        propService.delete(propId);
        return ResponseEntity.ok().build();
    }
}
