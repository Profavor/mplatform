package com.favorsoft.mplatform.batch;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalManager;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalService;
import com.favorsoft.mplatform.support.ApplicationContextProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalBatchService {

    private final ApprovalService approvalService;

    public ApprovalBatchService( ApprovalService approvalService){
        this.approvalService = approvalService;
    }

    //@Scheduled(cron = "0 * * * * ?")
    public void createApproval() throws Exception {
        List<Approval> approvalList = approvalService.getList(ApprovalStatus.CREATE);

        for(Approval approval: approvalList) {
            ApprovalFlow currentApprovalFlow = approval.getApprovalFlowList().stream().filter(s -> s.getSequence() == approval.getCurrentSequence()).findAny().get();
            ApprovalFlow nextApprovalFlow = null;
            if(currentApprovalFlow.getNextSequence() > 0){
                nextApprovalFlow = approval.getApprovalFlowList().stream().filter(s-> s.getSequence() == currentApprovalFlow.getNextSequence()).findAny().get();
            }

            currentApprovalFlow.setProcessDate(new Date());
            currentApprovalFlow.setApprovalFlowStatus(ApprovalFlowStatus.ACCEPT);
            approval.setStartDate(new Date());

            if(nextApprovalFlow != null){
                approval.setStatus(ApprovalStatus.PROCESS);
                approval.setCurrentSequence(nextApprovalFlow.getSequence());
                approval.setCurrentUser(nextApprovalFlow.getUser());
                nextApprovalFlow.setArriveDate(new Date());
            }

            ApprovalManager approvalManager = newInstance(approval.getRefClass());
            approvalManager.startApproval(approval);
            approvalService.save(approval);
        }
    }

    public void activeApproval() throws Exception {
        List<Approval> approvalList = approvalService.getList(ApprovalStatus.PROCESS);

        for(Approval approval: approvalList) {
            ApprovalFlow currentApprovalFlow = approval.getApprovalFlowList().stream().filter(s -> s.getSequence() == approval.getCurrentSequence()).findAny().get();
            ApprovalFlow nextApprovalFlow = null;
            if(currentApprovalFlow.getNextSequence() > 0){
                nextApprovalFlow = approval.getApprovalFlowList().stream().filter(s-> s.getSequence() == currentApprovalFlow.getNextSequence()).findAny().get();
            }

            ApprovalManager approvalManager = newInstance(approval.getRefClass());

            if(currentApprovalFlow.getApprovalFlowStatus().equals(ApprovalFlowStatus.ACCEPT)){
                if(nextApprovalFlow != null){
                    approval.setCurrentSequence(nextApprovalFlow.getSequence());
                    approval.setCurrentUser(nextApprovalFlow.getUser());
                    approvalManager.changedApproval(approval);
                }else{
                    approval.setStatus(ApprovalStatus.FINISH);
                    approval.setEndDate(new Date());
                    approvalManager.endApproval(approval);
                }
            }else if(currentApprovalFlow.getApprovalFlowStatus().equals(ApprovalFlowStatus.REJECT)){
                approval.setStatus(ApprovalStatus.REJECT);
                approval.setEndDate(new Date());
            }
            approvalService.save(approval);
        }
    }

    private ApprovalManager newInstance(String manageClass) throws Exception {
        return (ApprovalManager) ApplicationContextProvider.getApplicationContext().getBean(Class.forName(manageClass));
    }
}
