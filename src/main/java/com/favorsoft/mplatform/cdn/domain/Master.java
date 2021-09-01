package com.favorsoft.mplatform.cdn.domain;

import com.favorsoft.mplatform.cdn.domain.keys.MasterKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(MasterKey.class)
@NoArgsConstructor
public class Master extends BaseEntity {
    @Id
    private String masterId;

    @Id
    private String domainId;

    private String status;

    private String classId;

    public Master(String domainId, String masterId){
        this.domainId = domainId;
        this.masterId = masterId;
    }

    public Master(String domainId, String masterId, String classId){
        this.domainId = domainId;
        this.masterId = masterId;
        this.classId = classId;
    }
}
