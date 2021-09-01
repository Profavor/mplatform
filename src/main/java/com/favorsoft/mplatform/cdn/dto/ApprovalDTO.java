package com.favorsoft.mplatform.cdn.dto;

import lombok.Data;

@Data
public class ApprovalDTO {

    private String approvalTitle;
    private String approvalBody;
    private String additionalHtmlContent;
    private String apprComment;
    private String routeInfo;
    private String documentId;
    private String targetSystemUrl;
}
