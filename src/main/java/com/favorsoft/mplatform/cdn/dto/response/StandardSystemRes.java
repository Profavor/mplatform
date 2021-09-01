package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class StandardSystemRes {
    DomainRes domain;
    MclassRes mclass;
    List<MsectionRes> msections;

    public void addMsection(MsectionRes msectionRes) {
        this.msections.add(msectionRes);
    }
}
