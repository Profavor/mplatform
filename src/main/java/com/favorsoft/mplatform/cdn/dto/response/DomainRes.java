package com.favorsoft.mplatform.cdn.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DomainRes {
    private String domainId;
    private String message;
}
