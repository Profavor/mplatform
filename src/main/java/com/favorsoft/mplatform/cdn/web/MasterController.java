package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.dto.request.StandardSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.StandardSystemRes;
import com.favorsoft.mplatform.cdn.service.MasterCodeService;
import com.favorsoft.mplatform.cdn.service.MclassService;
import com.favorsoft.mplatform.cdn.service.MasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/master")
public class MasterController {

    protected Logger logger = LoggerFactory.getLogger(MasterController.class);

    private final MasterService masterService;
    private final MclassService mclassService;
    private final MasterCodeService masterCodeService;

    public MasterController(MasterService masterService, MclassService mclassService, MasterCodeService masterCodeService) {
        this.masterService = masterService;
        this.mclassService = mclassService;
        this.masterCodeService = masterCodeService;
    }

    @PostMapping
    public boolean saveMaster(@RequestBody @Valid MasterDTO masterDTO) {
        masterService.saveMaster(masterDTO);
        return true;
    }

    @GetMapping(value = "/form/{domainId}/{classId}")
    public StandardSystemRes getUI(StandardSystemReq standardSystemReq){
        return mclassService.getStandardSystemByClassAndDomain(standardSystemReq);
    }

    @GetMapping(value = "/getData/{domainId}/{classId}")
    public List<Map<String, Object>> getMasterData(MasterDTO masterDTO) throws Exception {
        return masterService.getMasterData(masterDTO);
    }

    @GetMapping(value = "/getData/{domainId}/{classId}/{masterId}")
    public Map<String, Object> getMasterDataObject(MasterDTO masterDTO) throws Exception {
        if(!masterService.getMasterData(masterDTO).isEmpty()) {
            return masterService.getMasterData(masterDTO).get(0);
        }
        return null;
    }

    @DeleteMapping(value = "/{domainId}/{masterId}")
    public boolean deleteMasterDataObject(MasterDTO masterDTO) throws Exception {
        masterService.deleteMaster(masterDTO);
        return true;
    }
}
