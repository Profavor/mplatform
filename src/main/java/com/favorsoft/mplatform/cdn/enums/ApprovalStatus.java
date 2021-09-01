package com.favorsoft.mplatform.cdn.enums;

public enum ApprovalStatus {
    CREATE("CREATE"), READY("READY"), PROCESS("PROCESS"), REJECT("REJECT"), DATA_SAVING("DATA_SAVING"), DATA_ERROR("DATA_ERROR"), FINISH("FINISH")
    ;

    private final String approvalStatus;

    ApprovalStatus(String approvalStatus){
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }
}
