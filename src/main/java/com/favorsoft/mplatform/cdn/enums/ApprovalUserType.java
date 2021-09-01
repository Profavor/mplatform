package com.favorsoft.mplatform.cdn.enums;

public enum ApprovalUserType {
    DRAFT("DRAFT"),         //기안
    APPROVAL("APPROVAL"),   //결재
    AGREE("AGREE");         //합의

    private String approvalType;

    ApprovalUserType(String approvalType){
        this.approvalType = approvalType;
    }

    public String getApprovalType(){
        return approvalType;
    }
}
