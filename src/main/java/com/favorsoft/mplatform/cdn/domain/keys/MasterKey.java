package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;

import java.io.Serializable;

@Data
public class MasterKey implements Serializable {

    private String masterId;

    private String domainId;

    public MasterKey() {
    }

    public MasterKey(String domainId, String masterId){
        this.domainId = domainId;
        this.masterId = masterId;
    }
}
