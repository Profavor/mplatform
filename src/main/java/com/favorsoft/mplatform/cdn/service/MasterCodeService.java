package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.MasterCode;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

public interface MasterCodeService extends CommonService<MasterCode> {
	public String createMasterId(String domainId, String classId);
	
}
