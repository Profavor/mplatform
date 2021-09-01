package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class MessageLangKey implements Serializable {
    @JoinColumn(table = "message", columnDefinition = "messageId")
    private String messageId;

    @Column(length = 100)
    private String lang;

    public MessageLangKey(String messageId, String lang){
        this.messageId = messageId; 
        this.lang = lang;
    }

}
