package com.favorsoft.mplatform.cdn.service.approval;

import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.List;

public interface ApprovalFlowService extends CommonService<ApprovalFlow> {
    List<ApprovalFlow> getList(User user, ApprovalActivityCode approvalActivityCode);
}
