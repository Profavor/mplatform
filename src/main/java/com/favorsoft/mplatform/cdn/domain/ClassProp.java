package com.favorsoft.mplatform.cdn.domain;

import com.favorsoft.mplatform.cdn.domain.keys.ClassPropKey;
import com.favorsoft.mplatform.cdn.dto.ClassPropDTO;
import com.favorsoft.mplatform.cdn.enums.PropMode;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@IdClass(ClassPropKey.class)
public class ClassProp extends BaseEntity {
    @Id
    private String classId;

    @Id
    private String domainId;

    @Id
    private String propId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PropMode propMode = PropMode.GENERAL;

    @Column(columnDefinition = "char(1) default 'N'")
    private String isReadOnly;

    @Column(columnDefinition = "char(1) default 'N'")
    private String isDisabled;

    private long dispSeq;

    @Column(columnDefinition = "char(1) default 'N'")
    private String isShow;

    @Builder
    public ClassProp(String classId, String domainId, String propId, String isReadOnly, String isDisabled, long dispSeq, String isShow, String propMode){
        this.classId = classId;
        this.domainId = domainId;
        this.propId = propId;
        this.isReadOnly = isReadOnly;
        this.isDisabled = isDisabled;
        this.dispSeq = dispSeq;
        this.isShow = isShow;
        this.propMode = PropMode.valueOf(propMode);
    }

    public static ClassPropBuilder fromClassPropDTO(ClassPropDTO classPropDTO){
        return ClassProp.builder()
                .classId(classPropDTO.getClassId())
                .domainId(classPropDTO.getDomainId())
                .propId(classPropDTO.getPropId())
                .isReadOnly(classPropDTO.getIsReadOnly())
                .isDisabled(classPropDTO.getIsDisabled())
                .dispSeq(classPropDTO.getDispSeq())
                .isShow(classPropDTO.getIsShow())
                .propMode(classPropDTO.getPropMode());
    }
}
