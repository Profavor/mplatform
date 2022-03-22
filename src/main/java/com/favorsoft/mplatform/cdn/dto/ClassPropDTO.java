package com.favorsoft.mplatform.cdn.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class ClassPropDTO {
    private String classId;
    private String domainId;
    private String propId;
    private String isReadOnly;
    private String isDisabled;
    private long dispSeq;
    private String isShow;
    private String propMode;

    @Builder
    public ClassPropDTO(String classId, String domainId, String propId, String isReadOnly, String isDisabled, long dispSeq, String isShow, String propMode){
        this.classId = classId;
        this.domainId = domainId;
        this.propId = propId;
        this.isReadOnly = isReadOnly;
        this.isDisabled = isDisabled;
        this.dispSeq = dispSeq;
        this.isShow = isShow;
        this.propMode = propMode;
    }
}
