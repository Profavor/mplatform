package com.favorsoft.mplatform.cdn.enums;

public enum ApprovalType {
    NORMAL("NORMAL"),
    SECRET("SECRET"),
    TOP_SECRET("TOP_SECRET"),
    ASAP("ASAP");

    private String approvalType;

    ApprovalType(String approvalType){
        this.approvalType = approvalType;
    }

    public String getApprovalType(){
        return approvalType;
    }
}
