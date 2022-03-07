package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CodeGroup extends BaseEntity {
    @Id
    private String codeGroupId;

    @Column(columnDefinition = "char(1) default 'N'")
    private String isEnable;

    @ManyToOne
    @JoinColumn(name="messageId", nullable= false)
    private Message message;

    private String description;

    @Column(length = 100)
    private String parentId;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "codeGroup")
    private List<CodeGroupProp> codeGroupProps = new ArrayList<>();

    public CodeGroup(String codeGroupId){
        this.codeGroupId =codeGroupId;
    }

    @Builder
    public CodeGroup(String codeGroupId, String isEnable, String messageId, String description){
        this.codeGroupId = codeGroupId;
        this.isEnable = isEnable;
        this.message = new Message(messageId);
        this.description = description;
    }

    public static CodeGroup.CodeGroupBuilder convertDTO(CodeGroupDTO dto){
        return CodeGroup.builder()
                .codeGroupId(dto.getCodeGroupId())
                .isEnable(dto.getIsEnable())
                .messageId(dto.getMessageId())
                .description(dto.getDescription());
    }
}
