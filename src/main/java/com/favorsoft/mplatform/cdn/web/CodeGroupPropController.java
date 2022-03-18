package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.CodeGroup;
import com.favorsoft.mplatform.cdn.domain.CodeGroupProp;
import com.favorsoft.mplatform.cdn.domain.Prop;
import com.favorsoft.mplatform.cdn.domain.keys.CodeGroupPropKey;
import com.favorsoft.mplatform.cdn.service.CodeGroupPropService;
import com.favorsoft.mplatform.cdn.service.CodeGroupService;
import com.favorsoft.mplatform.cdn.service.PropService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/codeGroupProps")
public class CodeGroupPropController {

    private final CodeGroupPropService codeGroupPropService;
    private final CodeGroupService codeGroupService;
    private final PropService propService;

    public CodeGroupPropController(CodeGroupPropService codeGroupPropService, CodeGroupService codeGroupService, PropService propService) {
        this.codeGroupPropService = codeGroupPropService;
        this.codeGroupService = codeGroupService;
        this.propService = propService;
    }

    @GetMapping
    public List<CodeGroupProp> getList() {
        return codeGroupPropService.getList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeGroupProp> save(@RequestBody CodeGroupProp codeGroupProp) {
        CodeGroupProp origin = codeGroupPropService.getObject(codeGroupProp.getId());
        BeanUtils.copyProperties(codeGroupProp, origin, CommonUtil.getNullPropertyNames(codeGroupProp));

        return ResponseEntity.ok(codeGroupPropService.save(origin));
    }

    /**
     * CodeGroupProp 가져오기
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}")
    public CodeGroupProp getObject(@PathVariable String id){
        return codeGroupPropService.getObject(Long.parseLong(id));
    }

    /**
     * CodeGroupProp 삭제
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{codeGroupId}")
    public ResponseEntity<CodeGroup> delete(@PathVariable String codeGroupId, @RequestParam String id) {
        codeGroupPropService.delete(Long.parseLong(id));
        return ResponseEntity.ok().build();
    }
}
