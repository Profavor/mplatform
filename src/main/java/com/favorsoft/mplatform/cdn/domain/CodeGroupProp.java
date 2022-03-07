package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.favorsoft.mplatform.cdn.enums.PropMode;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CodeGroupProp extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name= "propId")
    private Prop prop;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PropMode propMode = PropMode.GENERAL;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name= "codeGroupId")
    private CodeGroup codeGroup;

    @NonNull
    private int dispSeq;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isReadOnly;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isDisabled;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isShow;

    @NonNull
    @Column(columnDefinition = "char(1) default 'N'")
    private String isUnique;

}
