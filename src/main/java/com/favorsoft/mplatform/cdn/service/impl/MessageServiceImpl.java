package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Message;
import com.favorsoft.mplatform.cdn.repository.jpa.MessageRepository;
import com.favorsoft.mplatform.cdn.service.MessageService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(final MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Message> getList() {
        return messageRepository.findAll();
    }

    @Override
    public Message save(final Message message) {
        return messageRepository.saveAndFlush(message);
    }

    @Cacheable("getMessage")
    @Override
    public Message getObject(final Object key) {
        final String messageId = (String) key;
        return messageRepository.findById(messageId).orElse(new Message());
    }

    @Override
    public void delete(final Object key) {
        messageRepository.deleteById(String.valueOf(key));
    }
}
