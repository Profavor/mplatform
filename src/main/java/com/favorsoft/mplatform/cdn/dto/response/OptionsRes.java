package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class OptionsRes {
    private String propMode;
    private String isDisabled;
    private String isReadOnly;
    private String isShow;
    private String dispSeq;
}
