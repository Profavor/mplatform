package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;

import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
public class MasterKey implements Serializable {

    private String masterId;

    @JoinColumn(table = "domain", columnDefinition = "domainId")
    private String domainId;

    public MasterKey() {
    }

    public MasterKey(String domainId, String masterId){
        this.domainId = domainId;
        this.masterId = masterId;
    }
}
