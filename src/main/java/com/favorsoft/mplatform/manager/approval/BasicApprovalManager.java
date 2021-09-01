package com.favorsoft.mplatform.manager.approval;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalManager;
import org.springframework.stereotype.Component;

@Component
public class BasicApprovalManager implements ApprovalManager {
    @Override
    public void startApproval(Approval approval) {

    }

    @Override
    public void changedApproval(Approval approval) {

    }

    @Override
    public void endApproval(Approval approval) {

    }
}
