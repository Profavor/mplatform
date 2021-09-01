package com.favorsoft.mplatform.cdn.enums;

public enum ApprovalFlowStatus {
    DRAFT("DRAFT"),
    WAIT("WAIT"),
    ACCEPT("ACCEPT"),
    REJECT("REJECT");

    private String approvalFlowStatus;

    ApprovalFlowStatus(String approvalFlowStatus){
        this.approvalFlowStatus = approvalFlowStatus;
    }

    public String getApprovalFlowStatus(){
        return approvalFlowStatus;
    }
}
