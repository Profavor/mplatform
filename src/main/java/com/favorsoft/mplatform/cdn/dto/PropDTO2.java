package com.favorsoft.mplatform.cdn.dto;

import com.favorsoft.mplatform.cdn.enums.PropMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class PropDTO2 {
    private String propId;
    private String unit;
    private int width;
    private String regex;
    private String ruleCode;
    private String dbType;
    private String areaId;
    private String type;
    private String messageId;
    private String groupId;
    private String value;
    private String reference;
    private PropMode propMode;

    @Builder
    public PropDTO2(String propId, String unit, int width, String regex, String ruleCode, String dbType, String areaId, String type, String messageId, String groupId, String value, String reference, PropMode propMode) {
        this.propId = propId;
        this.unit = unit;
        this.width = width;
        this.regex = regex;
        this.ruleCode = ruleCode;
        this.dbType = dbType;
        this.areaId = areaId;
        this.type = type;
        this.messageId = messageId;
        this.groupId = groupId;
        this.value = value;
        this.reference = reference;
        this.propMode = propMode;
    }
}
