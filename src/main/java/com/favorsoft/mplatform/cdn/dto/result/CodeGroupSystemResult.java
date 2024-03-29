package com.favorsoft.mplatform.cdn.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CodeGroupSystemResult {
    private String id;
    private String codeGroupId;
    private String propId;
    private String areaId;
    private String type;
    private String unit;
    private String width;
    private String regex;
    private String ruleCode;
    private String mask;
    private String dbType;
    private String propMessage;
    private String propMode;
    private String isDisabled;
    private String isReadOnly;
    private String isShow;
    private String dispSeq;
    private String groupId;
    private String groupDispSeq;
    private String groupIsEnable;
    private String groupMessage;
    private String sectionId;
    private String sectionDispSeq;
    private String sectionIsEnable;
    private String sectionMessage;
}
