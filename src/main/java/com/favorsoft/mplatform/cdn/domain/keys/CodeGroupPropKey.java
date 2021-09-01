package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;

import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
public class CodeGroupPropKey implements Serializable {

    @JoinColumn(name = "codeGroupId")
    private String codeGroupId;

    @JoinColumn(name = "propId")
    private String propId;


    public CodeGroupPropKey() {}

    public CodeGroupPropKey(String codeGroupId, String propId){
        this.codeGroupId = codeGroupId;
        this.propId = propId;
    }
}
