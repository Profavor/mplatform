package com.classification.domain_system.event;

import com.classification.domain_system.entity.ApprovalRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApprovalRequestCancelledEvent {
    private final ApprovalRequest approvalRequest;
    private final String reason;
}
