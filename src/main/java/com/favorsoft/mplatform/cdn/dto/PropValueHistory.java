package com.favorsoft.mplatform.cdn.dto;

import com.favorsoft.mplatform.cdn.domain.BaseEntity;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class PropValueHistory extends BaseEntity {
    private final String propId;
    private final String domainId;
    private final String masterId;
    private final String areaId;
    private String seq;
    private final String value;
    private final String reference;

    public static PropValueHistory convert(PropValue propValue, String seq) {
        return builder()
                .propId(propValue.getPropId())
                .domainId(propValue.getDomainId())
                .masterId(propValue.getMasterId())
                .areaId(StringUtils.isEmpty(propValue.getAreaId()) ? "*" : propValue.getAreaId())
                .seq(seq)
                .value(propValue.getValue())
                .reference(propValue.getReference())
                .build();
    }
}
