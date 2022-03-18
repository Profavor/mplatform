package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.Mclass;
import com.favorsoft.mplatform.cdn.dto.request.StandardSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.StandardSystemRes;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.List;

public interface MclassService extends CommonService<Mclass> {
    StandardSystemRes getStandardSystemByClassAndDomain(StandardSystemReq standardSystemReq);
    List<Mclass> getList(String domainId);
    List<Mclass> getList(String domainId, String isEnable);
}
