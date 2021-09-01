package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Mclass;
import com.favorsoft.mplatform.cdn.domain.keys.MclassKey;
import com.favorsoft.mplatform.cdn.dto.MclassDTO;
import com.favorsoft.mplatform.cdn.dto.request.StandardSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.StandardSystemRes;
import com.favorsoft.mplatform.cdn.service.MclassService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도메인에 대한 분류
 * @author profavor
 */
@RestController
@RequestMapping(value = "/api/classes")
public class MclassController {

    private final MclassService mclassService;

    public MclassController(MclassService mclassService){
        this.mclassService = mclassService;
    }

    /**
     * Mclass List
     * @return
     */
    @GetMapping
    public List<Mclass> getList() {
        return mclassService.getList();
    }
    

    @GetMapping
    @RequestMapping("/{domainId}")
    public List<Mclass> getList(@PathVariable String domainId) {
        return mclassService.getList(domainId);
    }

    /**
     * Mclass 저장
     * @param mclassDTO
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mclass> save(@RequestBody MclassDTO mclassDTO) {
        Mclass mclass = Mclass.fromMclassDTO(mclassDTO).build();
        Mclass originMclass = mclassService.getObject(new MclassKey(mclassDTO.getDomainId(), mclassDTO.getClassId()));

        BeanUtils.copyProperties(mclass, originMclass, CommonUtil.getNullPropertyNames(mclass));

        return ResponseEntity.ok( mclassService.save(mclass));
    }

    /**
     * 분류(Mclass) 객체 로딩
     * @param domainId
     * @param classId
     * @return
     */
    @GetMapping(value = "/{domainId}/{classId}")
    public Mclass getObject(@PathVariable String domainId, @PathVariable String classId){
        return mclassService.getObject(new MclassKey(domainId, classId));
    }

    /**
     * Mclass 삭제
     * @param domainId
     * @param classId
     * @return
     */
    @DeleteMapping(value = "/{domainId}/{classId}")
    public ResponseEntity<Void> delete(@PathVariable String domainId, @PathVariable String classId) {
        mclassService.delete(new MclassKey(domainId, classId));
        return ResponseEntity.ok().build();

    }

    @GetMapping(value = "/standardSystem/{domainId}/{classId}")
    public  ResponseEntity<StandardSystemRes> getStandardSystemByClassAndDomain(StandardSystemReq standardSystemReq) {
        return ResponseEntity.ok(mclassService.getStandardSystemByClassAndDomain(standardSystemReq));
    }
}
