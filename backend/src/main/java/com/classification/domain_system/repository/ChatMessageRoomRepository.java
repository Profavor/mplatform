package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ChatMessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRoomRepository extends JpaRepository<ChatMessageRoom, UUID> {

    @Query("SELECT r FROM ChatMessageRoom r JOIN ChatMessageRoomMember m ON r.id = m.room.id WHERE m.userId = :userId ORDER BY r.lastMessageAt DESC")
    List<ChatMessageRoom> findRoomsByUserId(@Param("userId") String userId);
}
