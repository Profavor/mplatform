package com.favorsoft.mplatform.cdn.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Message extends BaseEntity {

    /**
     * Message
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100)
    private String messageId;

    @NotNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isEnable;

    @NotNull
    @Column(length = 300)
    private String messageKo;

    @NotNull
    @Column(length = 300)
    private String messageEn;

    public Message(String messageId){
        this.messageId = messageId;
    }
}
