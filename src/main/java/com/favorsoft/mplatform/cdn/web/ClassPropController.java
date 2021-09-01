package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.ClassProp;
import com.favorsoft.mplatform.cdn.domain.keys.ClassPropKey;
import com.favorsoft.mplatform.cdn.dto.ClassPropDTO;
import com.favorsoft.mplatform.cdn.service.ClassPropService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/classProps")
public class ClassPropController {

    private final ClassPropService classPropService;

    public ClassPropController(ClassPropService classPropService) {
        this.classPropService = classPropService;
    }

    /**
     * ClassProp List
     * @return
     */
    @GetMapping
    public List<ClassProp> getList() {
        return classPropService.getList();
    }

    /**
     * ClassProp 저장
     * @param ClassPropDTO
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClassProp> save(@RequestBody ClassPropDTO ClassPropDTO) {
        return ResponseEntity.ok(classPropService.save(ClassProp.fromClassPropDTO(ClassPropDTO).build()));
    }

    /**
     * Classses 객체 로딩
     * @param domainId
     * @param classId
     * @return
     */
    @GetMapping(value = "/{domainId}/{classId}/{propId}")
    public ClassProp getObject(@PathVariable String classId, @PathVariable String domainId, @PathVariable String propId){
        return classPropService.getObject(new ClassPropKey(classId, domainId, propId));
    }

    /**
     * ClassProp 삭제
     * @param domainId
     * @param classId
     * @return
     */
    @DeleteMapping(value = "/{domainId}/{classId}/{propId}")
    public ResponseEntity<Void> delete(@PathVariable String classId, @PathVariable String domainId, @PathVariable String propId){
        classPropService.delete(new ClassPropKey(classId, domainId, propId));
        return ResponseEntity.ok().build();
    }
}
