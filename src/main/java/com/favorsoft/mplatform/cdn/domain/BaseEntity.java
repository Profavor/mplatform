package com.favorsoft.mplatform.cdn.domain;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

@EntityListeners(value= {AuditingEntityListener.class})
@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {
    @CreatedBy
    private String creator;

    @CreatedDate
    private Date createdDate;

    @LastModifiedBy
    private String updater;

    @LastModifiedDate
    private Date updateDate;

    @Transient
    private String errorMessage;
}
