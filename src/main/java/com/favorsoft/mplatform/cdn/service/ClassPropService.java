package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.ClassProp;
import com.favorsoft.mplatform.cdn.enums.PropMode;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

public interface ClassPropService extends CommonService<ClassProp> {
    ClassProp getClassProp(String domainId, PropMode propMode);
}
