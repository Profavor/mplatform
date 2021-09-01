package com.favorsoft.mplatform.cdn.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class MasterCodeDTO {
    private String domainId;
    private String classId;
    private int length;
    private String prefix;
    private String suffix;
    private int cnt;

    @Builder
    public MasterCodeDTO(String domainId, String classId, int length, String prefix, String suffix, int cnt){
        this.domainId = domainId;
        this.classId = classId;
        this.length = length;
        this.prefix = prefix;
        this.suffix = suffix;
        this.cnt = cnt;
    }
}
