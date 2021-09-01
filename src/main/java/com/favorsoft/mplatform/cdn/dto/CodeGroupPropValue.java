package com.favorsoft.mplatform.cdn.dto;

import com.favorsoft.mplatform.cdn.domain.BaseEntity;
import lombok.Data;
import java.util.List;

@Data
public class CodeGroupPropValue extends BaseEntity {
    private String codeGroupId;
    private String code;
    private String name;

    List<PropValue> propValueList;

    @Data
    static class PropValue {
        private String propId;
        private String propValue;
    }
}
