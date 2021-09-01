package com.favorsoft.mplatform.cdn.service.approval;

import com.favorsoft.mplatform.cdn.domain.Approval;

public interface ApprovalManager {
    void startApproval(Approval approval);

    void changedApproval(Approval approval);

    void endApproval(Approval approval);
}
