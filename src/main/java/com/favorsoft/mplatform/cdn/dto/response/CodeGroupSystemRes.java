package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class CodeGroupSystemRes {
    CodeGroupRes codeGroup;
    List<MsectionRes> msections;

    public void addMsection(MsectionRes msectionRes) {
        this.msections.add(msectionRes);
    }
}
