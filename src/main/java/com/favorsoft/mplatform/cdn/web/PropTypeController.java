package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.PropType;
import com.favorsoft.mplatform.cdn.service.PropTypeService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/propTypes")
public class PropTypeController {
    private final PropTypeService propTypeService;

    public PropTypeController(PropTypeService propTypeService){
        this.propTypeService = propTypeService;
    }

    @GetMapping
    public List<PropType> getList() {
        return propTypeService.getList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropType> save(@RequestBody PropType propType) {
        PropType originPropType = propTypeService.getObject(propType.getType());

        BeanUtils.copyProperties(propType, originPropType, CommonUtil.getNullPropertyNames(propType));

        return ResponseEntity.ok(propTypeService.save(propType));
    }

    @GetMapping(value = "/{type}")
    public PropType getObject(@PathVariable String type){
        return propTypeService.getObject(type);
    }

    @DeleteMapping(value = "/{type}")
    public ResponseEntity delete(@PathVariable String type) {
        propTypeService.delete(type);
        return ResponseEntity.ok().build();
    }
}
