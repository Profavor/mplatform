package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(UUID roomId);

    List<ChatMessage> findByRoomIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(UUID roomId, LocalDateTime since);

    List<ChatMessage> findByCreatedAtBefore(LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.createdAt < :cutoff")
    int deleteMessagesOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") UUID roomId);
}
