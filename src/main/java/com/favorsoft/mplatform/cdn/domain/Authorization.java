package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Authorization extends BaseEntity {
    @Id
    @Column(length = 100)
    private String authId;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isRread;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isWrite;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isExecute;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name="messageId")
    @RestResource(path = "message", rel="message", exported = false)
    private Message message;
}
