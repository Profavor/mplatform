package com.favorsoft.mplatform.cdn.dto;

import com.favorsoft.mplatform.cdn.domain.Prop;
import lombok.*;
import org.springframework.util.StringUtils;

@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class PropValue {
    private String propId;
    private String domainId;
    private String classId;
    private String masterId;
    private String areaId;
    private String type;
    private String value;
    private String reference;

    @Builder
    public PropValue(String domainId, String propId, String classId, String areaId, String type, String masterId, String value){
        this.domainId = domainId;
        this.propId = propId;
        this.classId =classId;
        this.areaId = areaId;
        this.type= type;
        this.masterId = masterId;
        this.value = value;
    }

    public static PropValue convert(MasterDTO masterDTO, PropValue propValue) {
        return builder()
                .propId(propValue.getPropId())
                .domainId(masterDTO.getDomainId())
                .masterId(masterDTO.getMasterId())
                .areaId(StringUtils.hasText(propValue.getAreaId()) ? propValue.getAreaId() : "*")
                .value(propValue.getValue())
                .build();
    }

    public static PropValue convert(MasterDTO masterDTO, Prop prop) {
        return builder()
                .propId(prop.getPropId())
                .domainId(masterDTO.getDomainId())
                .masterId(masterDTO.getMasterId())
                .build();
    }
}
