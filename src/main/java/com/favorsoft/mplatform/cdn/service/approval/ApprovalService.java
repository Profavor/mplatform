package com.favorsoft.mplatform.cdn.service.approval;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.dto.ApprovalDTO;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.service.external.CommonService;
import java.util.List;

public interface ApprovalService extends CommonService<Approval> {
    List<Approval> getList(ApprovalStatus status);

    List<Approval> getList(User user, ApprovalStatus status);

    Approval create(ApprovalDTO approvalDTO);
}
