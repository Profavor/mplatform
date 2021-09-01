package com.favorsoft.mplatform.cdn.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ApprovalActivityCode {
    DRAFT("0"),
    APPROVAL("1"),
    AGREE("2"),
    POST_APPROVAL("3"),
    PARALLEL_AGREE("4"),
    PARALLEL_APPROVAL("7"),
    NOTIFICATION("9"),
    UNKNOWN("UNKNOWN");

    private String approvalActivityCode;

    ApprovalActivityCode(String approvalActivityCode){
        this.approvalActivityCode = approvalActivityCode;
    }

    public static ApprovalActivityCode find(String approvalActivityCode) {
        return Arrays.stream(values()) .filter(s -> s.approvalActivityCode.equals(approvalActivityCode)).findAny().orElse(UNKNOWN);
    }


}
