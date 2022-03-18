package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.favorsoft.mplatform.cdn.dto.MclassDTO;
import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Mclass extends BaseEntity {
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

    @ManyToOne
    @JoinColumn(name= "domainId", insertable = true, updatable = true)
    @RestResource(path = "domain", rel="domain", exported = false)
    private Domain domain;

    @JsonManagedReference
    @OneToMany
    @JoinColumn(name= "classId", insertable = false, updatable = false)
    @RestResource(path = "classProp", rel="classProp", exported = false)
    private List<ClassProp> classProp;

    private long dispSeq;

    @Builder
    public Mclass(String domainId, String classId, String isEnable, String parentId, String messageId, long dispSeq){
        this.domain = new Domain(domainId);
        this.classId = classId;
        this.isEnable = isEnable;
        this.parentId = parentId;
        this.message = new Message(messageId);
        this.dispSeq = dispSeq;
    }

    public static MclassBuilder fromMclassDTO(MclassDTO mclassDTO){
        return Mclass.builder()
                .classId(mclassDTO.getClassId())
                .domainId(mclassDTO.getDomainId())
                .isEnable(mclassDTO.getIsEnable())
                .parentId(mclassDTO.getParentId())
                .messageId(mclassDTO.getMessageId())
                .dispSeq(mclassDTO.getDispSeq());
    }

}
