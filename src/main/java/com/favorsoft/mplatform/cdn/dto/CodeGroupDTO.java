package com.favorsoft.mplatform.cdn.dto;

import com.favorsoft.mplatform.cdn.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CodeGroupDTO extends BaseEntity {
    private String codeGroupId;
    private String description;
    private String isEnable;
    private String messageId;

    private String code;
    private String name;

    private String reference;
    private List<PropValue> data;

    @Builder
    public CodeGroupDTO(String codeGroupId, String description, String isEnable, String messageId, String code, String name, String reference, List<PropValue> data){
        this.codeGroupId = codeGroupId;
        this.description = description;
        this.isEnable = isEnable == null ? "N" : isEnable;
        this.messageId = messageId;
        this.code = code;
        this.name = name;
        this.reference = reference;
        this.data = data;
    }
}
