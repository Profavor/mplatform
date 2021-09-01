package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;

import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
public class ClassPropKey implements Serializable {
    @JoinColumn(name = "classId")
    private String classId;

    @JoinColumn(name = "domainId")
    private String domainId;

    @JoinColumn(name = "propId")
    private String propId;

    public ClassPropKey() {

    }

    public ClassPropKey(String classId, String domainId, String propId){
        this.classId = classId;
        this.domainId = domainId;
        this.propId = propId;
    }
}
