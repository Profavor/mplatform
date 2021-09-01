package com.favorsoft.mplatform.cdn.domain;

import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PropType extends BaseEntity {
    @Id
    @Column(length = 100)
    private String type;

    @NonNull
    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name="messageId")
    @RestResource(path = "message", rel="message", exported = false)
    private Message message;

    public PropType(String type) {
        this.type = type;
    }
}
