package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@RestController
@RequestMapping("/api/approval/flow")
public class ApprovalFlowController {

    private final ApprovalFlowService approvalFlowService;

    public ApprovalFlowController(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }

    @GetMapping("/getApprovalList/{activityCode}")
    public ResponseEntity getList(@PathVariable String activityCode){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        List<ApprovalFlow> approvalFlowList = approvalFlowService.getList(user, ApprovalActivityCode.valueOf(activityCode));
        List<Approval> approvalList = new ArrayList<>();
        for(ApprovalFlow approvalFlow: approvalFlowList){
            approvalList.add(approvalFlow.getApproval());
        }
        Queue<ApprovalFlow> q = new LinkedList<ApprovalFlow>();

        return ResponseEntity.ok(approvalList);
    }

}
