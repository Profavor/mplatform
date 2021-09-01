package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.MasterCode;
import com.favorsoft.mplatform.cdn.domain.keys.MasterCodeKey;
import com.favorsoft.mplatform.cdn.dto.MasterCodeDTO;
import com.favorsoft.mplatform.cdn.service.MasterCodeService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 복합키를 가지는 Entity에 대하여 별도 설정 (필요한 부분 직접 구현)
 * 분류 MasterCode Rest Controller
 */
@RestController
@RequestMapping(value = "/api/masterCodes")
public class MasterCodeController {

    private final MasterCodeService masterCodeService;

    public MasterCodeController(MasterCodeService masterCodeService){
        this.masterCodeService = masterCodeService;
    }

    /**
     * MasterCode List
     * @return
     */
    @GetMapping
    public List<MasterCode> getList() {
        return masterCodeService.getList();
    }

    /**
     * MasterCode 저장
     * @param masterCodeDTO
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MasterCode> save(@RequestBody MasterCodeDTO masterCodeDTO) {
        MasterCode masterCode = MasterCode.fromMasterCodeDTO(masterCodeDTO).build();
        MasterCode originMasterCode = masterCodeService.getObject(new MasterCodeKey(masterCodeDTO.getDomainId(), masterCodeDTO.getClassId()));

        BeanUtils.copyProperties(masterCode, originMasterCode, CommonUtil.getNullPropertyNames(masterCode));

        return ResponseEntity.ok(masterCodeService.save(masterCode));
    }

    /**
     * Classses 객체 로딩
     * @param domainId
     * @param classId
     * @return
     */
    @GetMapping(value = "/{domainId}/{classId}")
    public MasterCode getObject(@PathVariable String domainId, @PathVariable String classId){
        return masterCodeService.getObject(new MasterCodeKey(domainId, classId));
    }

    /**
     * MasterCode 삭제
     * @param domainId
     * @param classId
     * @return
     */
    @DeleteMapping(value = "/{domainId}/{classId}")
    public ResponseEntity<Void> delete(@PathVariable String domainId, @PathVariable String classId) {
        masterCodeService.delete(new MasterCodeKey(domainId, classId));
        return ResponseEntity.ok().build();
    }
}
