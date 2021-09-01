package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Mgroup;
import com.favorsoft.mplatform.cdn.service.MgroupService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/groups")
public class MgroupController {
    private final MgroupService mgroupService;

    public MgroupController(MgroupService mgroupService){
        this.mgroupService = mgroupService;
    }

    @GetMapping
    public List<Mgroup> getList() {
        return mgroupService.getList();
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mgroup> save(@RequestBody Mgroup mgroup) {
        Mgroup originMgroup = mgroupService.getObject(mgroup.getGroupId());

        BeanUtils.copyProperties(mgroup, originMgroup, CommonUtil.getNullPropertyNames(mgroup));

        return ResponseEntity.ok(mgroupService.save(mgroup));
    }

    @GetMapping(value = "/{groupId}")
    public Mgroup getObject(@PathVariable String groupId){
        return mgroupService.getObject(groupId);
    }

    @DeleteMapping(value = "/{groupId}")
    public ResponseEntity delete(@PathVariable String groupId) {
        mgroupService.delete(groupId);
        return ResponseEntity.ok().build();
    }
}
