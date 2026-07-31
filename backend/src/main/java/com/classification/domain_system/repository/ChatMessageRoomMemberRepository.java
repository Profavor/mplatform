package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ChatMessageRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageRoomMemberRepository extends JpaRepository<ChatMessageRoomMember, UUID> {

    List<ChatMessageRoomMember> findByRoomId(UUID roomId);

    Optional<ChatMessageRoomMember> findByRoomIdAndUserId(UUID roomId, String userId);
}
