package com.favorsoft.mplatform.cdn.domain;

import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Area extends BaseEntity {
    @Id
    @Column(length = 100)
    private String areaId;

    @ManyToOne
    @JoinColumn(name="messageId")
    @RestResource(path = "message", rel="message", exported = false)
    private Message message;

    @NonNull
    @Column(length = 200)
    private String status;

    public Area(String areaId) {
        this.areaId = areaId;
    }
}
