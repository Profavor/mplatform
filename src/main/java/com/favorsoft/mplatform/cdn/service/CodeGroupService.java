package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.CodeGroup;
import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.request.CodeGroupSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.CodeGroupSystemRes;
import com.favorsoft.mplatform.cdn.service.external.CommonService;

import java.util.Map;
import java.util.List;

public interface CodeGroupService extends CommonService<CodeGroup> {

    List<Map<String, Object>> getCodeList(String codeGroupId);

    Map<String, Object> getCode(String codeGroupId, String code);

    void saveCode(CodeGroupDTO codeGroupDTO);

    void deleteCode(String codeGroupId, String code);

    CodeGroupSystemRes getFormatedFormUI(CodeGroupSystemReq codeGroupSystemReq);
}
