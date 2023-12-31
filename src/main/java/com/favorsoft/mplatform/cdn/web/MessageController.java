package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Message;
import com.favorsoft.mplatform.cdn.service.MasterCodeService;
import com.favorsoft.mplatform.cdn.service.MessageService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/messages")
public class MessageController {

    private final MessageService messageService;

    private final MasterCodeService masterCodeService;

    public MessageController(final MessageService messageService, final MasterCodeService masterCodeService) {
        this.messageService = messageService;
        this.masterCodeService = masterCodeService;
    }

    @GetMapping
    public List<Message> getList() {
        return messageService.getList();
    }

    /**
     * Message 저장
     * @param message
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Message> save(@RequestBody @Valid Message message) {
        if(null == message.getMessageId()){
            String key = UUID.randomUUID().toString();
            message.setMessageId(key);
        }
        return ResponseEntity.ok(messageService.save(message));
    }
    
    @GetMapping(value = "/{messageId}")
    public Message getObject(@PathVariable final String messageId){
        return messageService.getObject(messageId);
    }

    @DeleteMapping(value = "/{messageId}")
    public ResponseEntity delete(@PathVariable final String messageId){
        messageService.delete(messageId);
        return ResponseEntity.ok().build();
    }
}
