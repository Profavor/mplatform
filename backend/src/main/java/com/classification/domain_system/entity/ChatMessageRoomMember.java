package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_message_room_member")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatMessageRoom room;

    @Column(nullable = false)
    private String userId;

    private LocalDateTime joinedAt = LocalDateTime.now();

    private LocalDateTime lastReadAt = LocalDateTime.now();
}
