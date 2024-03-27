package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MclassRes {
    private String classId;
    private String message;
    private String classPath;
}
