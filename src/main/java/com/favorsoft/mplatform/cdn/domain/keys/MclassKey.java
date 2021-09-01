package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
public class MclassKey implements Serializable {

    @JoinColumn(table = "domain", columnDefinition = "domainId")
    private String domainId;

    @Column(length = 100)
    private String classId;

    public MclassKey() { }

    public MclassKey(String domainId, String classId){
        this.domainId = domainId;
        this.classId = classId;
    }
}
