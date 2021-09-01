package com.favorsoft.mplatform.cdn.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Msection extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100)
    private String sectionId;

    private long dispSeq;

    @NonNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isEnable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name= "messageId", insertable = true, updatable = true, nullable = false)
    private Message message;
}
