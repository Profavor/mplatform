package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.favorsoft.mplatform.cdn.domain.keys.MessageLangKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(MessageLangKey.class)
public class MessageLang extends BaseEntity {
    @Id
    private String messageId;

    @Id
    private String lang;

    @Column(length = 300)
    private String message;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name= "messageId", insertable = false, updatable = false)
    private Message messageObject;
}
