package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ChatMessageRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ChatMessageRoomMemberRepository extends JpaRepository<ChatMessageRoomMember, UUID> {

    List<ChatMessageRoomMember> findByRoomId(UUID roomId);

    Optional<ChatMessageRoomMember> findByRoomIdAndUserId(UUID roomId, String userId);

    @Modifying
    @Query("DELETE FROM ChatMessageRoomMember m WHERE m.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") UUID roomId);

    @Modifying
    @Query("DELETE FROM ChatMessageRoomMember m WHERE m.room.id = :roomId AND m.userId = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") UUID roomId, @Param("userId") String userId);
}
