package com.favorsoft.mplatform.cdn.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class MclassDTO {
    private String domainId;
    private String classId;
    private String parentId;
    private String isEnable;
    private String messageId;
    private long dispSeq;

    @Builder
    public MclassDTO(String domainId, String classId, String parentId, String isEnable, String messageId, long dispSeq){
        this.domainId = domainId;
        this.classId = classId;
        this.parentId = parentId;
        this.isEnable = isEnable == null ? "N" : isEnable;
        this.messageId = messageId;
        this.dispSeq = dispSeq;
    }
}
