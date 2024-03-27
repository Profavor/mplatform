package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Domain;
import com.favorsoft.mplatform.cdn.service.DomainService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/domains")
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService){
        this.domainService = domainService;
    }

    @GetMapping
    public List<Domain> getList() {
        return domainService.getList();
    }    

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Domain> save(@RequestBody Domain domain) {
        Domain originDomain = domainService.getObject(domain.getDomainId());

        BeanUtils.copyProperties(domain, originDomain, CommonUtil.getNullPropertyNames(domain));

        return ResponseEntity.ok(domainService.save(originDomain));
    }

    /**
     * Domain 가져오기
     * @param domainId
     * @return
     */
    @GetMapping(value = "/{domainId}")
    public Domain getObject(@PathVariable String domainId){
        return domainService.getObject(domainId);
    }

    /**
     * Domain 삭제
     * @param domainId
     * @return
     */
    @DeleteMapping(value = "/{domainId}")
    public ResponseEntity<Domain> delete(@PathVariable String domainId) {
        domainService.delete(domainId);
        return ResponseEntity.ok().build();
    }
}
