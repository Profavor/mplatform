package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private String senderId;

    private String senderName;

    @Column(nullable = false)
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE, EMOJI

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;

    private String fileName;

    private Long fileSize;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Version
    private Long version;
}
