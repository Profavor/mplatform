package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.ChatMessage;
import com.classification.domain_system.entity.ChatMessageRoom;
import com.classification.domain_system.exception.CustomAccessDeniedException;
import com.classification.domain_system.service.ChatMessageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.classification.domain_system.service.storage.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final AuthContext authContext;
    private final com.classification.domain_system.websocket.PresenceEventListener presenceEventListener;
    private final FileStorageService fileStorageService;

    @GetMapping("/presence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Set<String>> getOnlineUsers() {
        return ResponseEntity.ok(presenceEventListener.getOnlineUsers());
    }

    private String getAuthenticatedUserId() {
        String uid = authContext.getUserId();
        if (uid == null || uid.isBlank()) {
            throw new CustomAccessDeniedException("Unauthenticated user");
        }
        return uid;
    }

    @Data
    public static class CreateRoomRequest {
        private String roomName;
        private Boolean isGroup;
        private List<String> memberUserIds;
    }

    @Data
    public static class SendMessageRequest {
        private UUID roomId;
        private String senderId;
        private String messageType; // TEXT, IMAGE, FILE, EMOJI
        private String content;
        private String fileUrl;
        private String fileName;
        private Long fileSize;
    }

    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageRoom>> getMyRooms() {
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(chatMessageService.getUserRooms(userId));
    }

    @PostMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageRoom> createRoom(@RequestBody CreateRoomRequest req) {
        String creatorId = getAuthenticatedUserId();
        String name = (req.getRoomName() != null && !req.getRoomName().isBlank()) 
                ? req.getRoomName() 
                : "대화방";
        return ResponseEntity.ok(chatMessageService.createRoom(name, req.getIsGroup(), creatorId, req.getMemberUserIds()));
    }

    @GetMapping("/rooms/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageService.RoomMemberDto>> getRoomMembers(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatMessageService.getRoomMembers(roomId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageService.ChatMessageDto>> getRoomMessages(@PathVariable UUID roomId) {
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(chatMessageService.getRoomMessages(roomId, userId));
    }

    @PostMapping("/rooms/{roomId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markRoomAsRead(@PathVariable UUID roomId) {
        String userId = getAuthenticatedUserId();
        chatMessageService.markRoomAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> leaveRoom(@PathVariable UUID roomId) {
        String userId = getAuthenticatedUserId();
        chatMessageService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteRoom(@PathVariable UUID roomId) {
        String userId = getAuthenticatedUserId();
        chatMessageService.deleteRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class DelegateCreatorRequest {
        private String newCreatorId;
    }

    @PutMapping("/rooms/{roomId}/creator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delegateCreator(@PathVariable UUID roomId, @RequestBody DelegateCreatorRequest req) {
        String userId = getAuthenticatedUserId();
        chatMessageService.delegateCreator(roomId, userId, req.getNewCreatorId());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class InviteMembersRequest {
        private List<String> userIds;
        private Integer pastMessageHours;
    }

    @PostMapping("/rooms/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> inviteMembers(@PathVariable UUID roomId, @RequestBody InviteMembersRequest req) {
        String userId = getAuthenticatedUserId();
        Integer hours = req.getPastMessageHours() != null ? req.getPastMessageHours() : 0;
        if (hours > 48) hours = 48;
        chatMessageService.inviteMembers(roomId, userId, req.getUserIds(), hours);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/members/{targetUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> kickMember(@PathVariable UUID roomId, @PathVariable String targetUserId) {
        String userId = getAuthenticatedUserId();
        chatMessageService.kickMember(roomId, userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getTotalUnreadCount() {
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(chatMessageService.getTotalUnreadCount(userId));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageService.ChatMessageDto> sendMessageREST(@PathVariable UUID roomId, @RequestBody SendMessageRequest req) {
        String senderId = getAuthenticatedUserId();
        ChatMessageService.ChatMessageDto msg = chatMessageService.sendMessage(
                roomId,
                senderId,
                req.getMessageType(),
                req.getContent(),
                req.getFileUrl(),
                req.getFileName(),
                req.getFileSize()
        );
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> uploadChatFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String origName = file.getOriginalFilename();
            String savedFileName = fileStorageService.storeFile(file);

            String fileUrl = "/api/chat/files/" + savedFileName;
            return ResponseEntity.ok(Map.of(
                    "fileUrl", fileUrl,
                    "fileName", origName != null ? origName : savedFileName,
                    "fileSize", file.getSize()
            ));
        } catch (Exception e) {
            log.error("Failed to upload chat file", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/files/{fileName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getChatFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                    .or(() -> MediaTypeFactory.getMediaType(resource))
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve chat file: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMessage(@PathVariable UUID messageId) {
        String userId = getAuthenticatedUserId();
        chatMessageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat.send")
    public void sendMessageSTOMP(@Payload SendMessageRequest req) {
        if (req != null && req.getRoomId() != null && req.getSenderId() != null) {
            chatMessageService.sendMessage(
                    req.getRoomId(),
                    req.getSenderId(),
                    req.getMessageType(),
                    req.getContent(),
                    req.getFileUrl(),
                    req.getFileName(),
                    req.getFileSize()
            );
        }
    }
}
