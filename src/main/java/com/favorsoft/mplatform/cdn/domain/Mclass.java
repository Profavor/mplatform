package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.favorsoft.mplatform.cdn.domain.keys.MclassKey;
import com.favorsoft.mplatform.cdn.dto.MclassDTO;
import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(MclassKey.class)
public class Mclass extends BaseEntity {
    @Id
    private String domainId;

    @Id
    private String classId;

    @NonNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isEnable;

    @Column(length = 100)
    private String parentId;

    @ManyToOne
    @JoinColumn(name="messageId")
    @RestResource(path = "message", rel="message", exported = false)
    private Message message;

    @JsonManagedReference
    @OneToMany
    @JoinColumns({
            @JoinColumn(name = "classId"),
            @JoinColumn(name = "domainId")
    })

    @RestResource(path = "classProp", rel="classProp", exported = false)
    private List<ClassProp> classProp;

    @Builder
    public Mclass(String domainId, String classId, String isEnable, String parentId, String messageId){
        this.domainId = domainId;
        this.classId = classId;
        this.isEnable = isEnable;
        this.parentId = parentId;
        this.message = new Message(messageId);
    }

    public static MclassBuilder fromMclassDTO(MclassDTO mclassDTO){
        return Mclass.builder()
                .classId(mclassDTO.getClassId())
                .domainId(mclassDTO.getDomainId())
                .isEnable(mclassDTO.getIsEnable())
                .parentId(mclassDTO.getParentId())
                .messageId(mclassDTO.getMessageId());
    }

}
