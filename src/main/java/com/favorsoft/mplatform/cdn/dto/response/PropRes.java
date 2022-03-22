package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PropRes {
    private String propId;
    private String areaId;
    private String type;
    private String unit;
    private String width;
    private String regex;
    private String ruleCode;
    private String mask;
    private String dbType;
    private String message;
    private String reference;

    private OptionsRes options;
}
