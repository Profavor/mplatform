package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.PropEnum;
import com.favorsoft.mplatform.cdn.domain.keys.PropEnumKey;
import com.favorsoft.mplatform.cdn.dto.ResponseDTO;
import com.favorsoft.mplatform.cdn.service.PropEnumService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/propEnums")
public class PropEnumController {
    private final PropEnumService propEnumService;

    public PropEnumController(PropEnumService propEnumService){
        this.propEnumService = propEnumService;
    }

    @GetMapping
    public List<PropEnum> getList() {
        return propEnumService.getList();
    }

    @GetMapping(value = "/{propId}")
    public List<PropEnum> getListByPropId(@PathVariable String propId) {
        return propEnumService.getListByPropId(propId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropEnum> save(@RequestBody PropEnum propEnum) {
        PropEnum originPropEnum = propEnumService.getPropEnum(propEnum.getPropId(), propEnum.getCode());

        BeanUtils.copyProperties(propEnum, originPropEnum, CommonUtil.getNullPropertyNames(propEnum));

        return ResponseEntity.ok(propEnumService.save(propEnum));
    }

    @GetMapping(value = "/{propId}/{code}")
    public PropEnum getObject(@PathVariable String propId, @PathVariable String code){
        return propEnumService.getObject(new PropEnumKey(propId, code));
    }

    @DeleteMapping(value = "/{propId}/{code}")
    public ResponseDTO<?> delete(@PathVariable String propId, @PathVariable String code) {
        ResponseDTO<?> responseDto = null;
        try{
            propEnumService.delete(new PropEnumKey(propId, code));
            responseDto = ResponseDTO.builder()
                    .statusCode(HttpStatus.OK)
                    .message("Delete success")
                    .build();
        }catch(Exception e){
            responseDto = ResponseDTO.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Delete fail: "+e.getMessage())
                    .object(e.getCause())
                    .build();
        }
        return responseDto;
    }
}
