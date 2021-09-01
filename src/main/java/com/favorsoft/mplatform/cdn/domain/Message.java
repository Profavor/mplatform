package com.favorsoft.mplatform.cdn.domain;

import lombok.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Message extends BaseEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100)
    private String messageId;

    @NonNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isEnable;

    @JsonManagedReference
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "messageObject", fetch = FetchType.EAGER)
    private List<MessageLang> messageLangs;

    public Message(String messageId){
        this.messageId = messageId;
    }
}
