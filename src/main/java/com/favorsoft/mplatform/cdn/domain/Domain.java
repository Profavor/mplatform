package com.favorsoft.mplatform.cdn.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class Domain extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100)
    private String domainId;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isEnable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name= "messageId", insertable = true, updatable = true, nullable = false)
    private Message message;

    public Domain(){

    }

    public Domain(String domainId) {
        this.domainId = domainId;
    }

}
