package com.favorsoft.mplatform.cdn.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.rest.core.annotation.RestResource;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Mgroup extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100)
    private String groupId;

    private long dispSeq;

    private boolean expand;

    @NonNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isEnable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name= "messageId", insertable = true, updatable = true, nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "sectionId")
    @RestResource(path = "msection", rel="msection", exported = false)
    private Msection msection;

    public Mgroup(String groupId) {
        this.groupId = groupId;
    }
}
