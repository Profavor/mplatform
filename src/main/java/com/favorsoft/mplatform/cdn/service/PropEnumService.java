package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.PropEnum;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.List;

public interface PropEnumService  extends CommonService<PropEnum> {

    List<PropEnum> getListByPropId(String propId);

    PropEnum getPropEnum(String propId, String code);
}
