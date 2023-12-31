package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.CodeGroup;
import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.dto.request.CodeGroupSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.CodeGroupSystemRes;
import com.favorsoft.mplatform.cdn.service.CodeGroupService;
import com.favorsoft.mplatform.support.CommonUtil;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/codeGroups")
public class CodeGroupController {

    private final CodeGroupService codeGroupService;

    public CodeGroupController(CodeGroupService codeGroupService){
        this.codeGroupService = codeGroupService;
    }

    @GetMapping
    public List<CodeGroup> getList() {
        return codeGroupService.getList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeGroup> save(@RequestBody CodeGroupDTO codeGroupDTO) {
        CodeGroup codeGroup = CodeGroup.convertDTO(codeGroupDTO).build();
        CodeGroup origin = codeGroupService.getObject(codeGroup.getCodeGroupId());

        BeanUtils.copyProperties(codeGroup, origin, CommonUtil.getNullPropertyNames(codeGroup));

        return ResponseEntity.ok(codeGroupService.save(origin));
    }

    /**
     * CodeGroup 가져오기
     * @param codeGroupId
     * @return
     */
    @GetMapping(value = "/{codeGroupId}")
    public CodeGroup getObject(@PathVariable String codeGroupId){
        return codeGroupService.getObject(codeGroupId);
    }

    /**
     * CodeGroup 삭제
     * @param codeGroupId
     * @return
     */
    @DeleteMapping(value = "/{codeGroupId}")
    public ResponseEntity<CodeGroup> delete(@PathVariable String codeGroupId) {
        codeGroupService.delete(codeGroupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="/form/{codeGroupId}")
    public ResponseEntity<CodeGroupSystemRes> getFormUI(@PathVariable String codeGroupId, @RequestParam String lang){
        CodeGroupSystemReq codeGroupSystemReq = new CodeGroupSystemReq();
        codeGroupSystemReq.setCodeGroupId(codeGroupId);
        codeGroupSystemReq.setLang(lang);
        return ResponseEntity.ok(codeGroupService.getFormatedFormUI(codeGroupSystemReq));

    }

    @GetMapping(value="/data/{codeGroupId}")
    public ResponseEntity<List<Map<String, Object>>> getCodeList(@PathVariable String codeGroupId){
        return ResponseEntity.ok(codeGroupService.getCodeList(codeGroupId));
    }

    @GetMapping(value="/data/{codeGroupId}/{code}")
    public ResponseEntity<Map<String, Object>> getCode(@PathVariable String codeGroupId, @PathVariable String code){
        return ResponseEntity.ok(codeGroupService.getCode(codeGroupId, code));
    }

    @PostMapping(value="/saveCode")
    public ResponseEntity saveCode(@RequestBody @Valid CodeGroupDTO codeGroupDTO){
        codeGroupService.saveCode(codeGroupDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="/deleteCode/{codeGroupId}/{code}")
    public ResponseEntity deleteCode(@PathVariable String codeGroupId, @PathVariable String code){
        codeGroupService.deleteCode(codeGroupId, code);
        return ResponseEntity.ok().build();
    }

}
