package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class MgroupRes {
    private String groupId;
    private String dispSeq;
    private String isEnable;
    private String message;

    private List<PropRes> props;

    public void addProp(PropRes propRes) {
        this.props.add(propRes);
    }
}
