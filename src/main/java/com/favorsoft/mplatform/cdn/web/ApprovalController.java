package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.dto.ApprovalDTO;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService){
        this.approvalService = approvalService;
    }

    @GetMapping("/getList/{type}")
    public ResponseEntity<List<Approval>> getApprovalList(@PathVariable String type){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(approvalService.getList(user, ApprovalStatus.valueOf(type)));
    }

    @PostMapping("/create")
    public ResponseEntity<Approval> create(@RequestBody ApprovalDTO approvalDTO) {

        Approval approval = approvalService.create(approvalDTO);

        return ResponseEntity.ok(approval);
    }

    @PostMapping("/accept")
    public ResponseEntity accept(@RequestBody ApprovalFlow approvalFlow) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Approval appr = approvalService.getObject(approvalFlow.getApproval().getApprovalId());
        ApprovalFlow apprFlow = appr.getApprovalFlowList().stream().filter(s-> s.getUser().getUserId().equals(user.getUserId())).findFirst().orElseThrow(() -> new Exception("Not found approval user"));

        apprFlow.setApprovalFlowStatus(ApprovalFlowStatus.ACCEPT);
        apprFlow.setProcessDate(new Date());
        apprFlow.setOpinion(approvalFlow.getOpinion());

        ApprovalFlow nextFlow = appr.getApprovalFlowList().stream().filter(s-> s.getSequence() == apprFlow.getNextSequence()).findFirst().orElseThrow(() -> new Exception("Not found approval flow sequence"));
        nextFlow.setArriveDate(new Date());

        appr.setCurrentUser(nextFlow.getUser());
        appr.setCurrentSequence(nextFlow.getSequence());

        approvalService.save(appr);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity reject(@RequestBody ApprovalFlow approvalFlow) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Approval appr = approvalService.getObject(approvalFlow.getApproval().getApprovalId());
        ApprovalFlow apprFlow = appr.getApprovalFlowList().stream().filter(s-> s.getUser().getUserId().equals(user.getUserId())).findFirst().orElseThrow(() -> new Exception("Not found approval user"));

        apprFlow.setApprovalFlowStatus(ApprovalFlowStatus.REJECT);
        apprFlow.setProcessDate(new Date());
        apprFlow.setOpinion(approvalFlow.getOpinion());

        appr.setEndDate(new Date());
        appr.setStatus(ApprovalStatus.REJECT);

        approvalService.save(appr);

        return ResponseEntity.ok().build();
    }
}
