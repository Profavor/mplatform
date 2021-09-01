package com.favorsoft.mplatform.cdn.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CodeGroupPropDTO {
    private Long id;
    private String codeGroupId;
    private String propId;
    private String isReadOnly;
    private String isDisabled;
    private int dispSeq;
    private String isShow;

    @Builder
    public CodeGroupPropDTO(Long id, String codeGroupId, String propId, String isReadOnly, String isDisabled, int dispSeq, String isShow){
        this.id = id;
        this.codeGroupId = codeGroupId;
        this.propId = propId;
        this.isReadOnly = isReadOnly;
        this.isDisabled = isDisabled;
        this.dispSeq = dispSeq;
        this.isShow = isShow;
    }
}
