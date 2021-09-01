package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class MsectionRes {
    private String sectionId;
    private String dispSeq;
    private String isEnable;
    private String message;

    private List<MgroupRes> mgroups;

    public void addMgroup(MgroupRes mgroupRes) {
        this.mgroups.add(mgroupRes);
    }
}
