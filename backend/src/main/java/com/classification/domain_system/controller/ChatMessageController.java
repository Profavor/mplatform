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

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
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
        return ResponseEntity.ok(chatMessageService.getRoomMessages(roomId));
    }

    @PostMapping("/rooms/{roomId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markRoomAsRead(@PathVariable UUID roomId) {
        String userId = getAuthenticatedUserId();
        chatMessageService.markRoomAsRead(roomId, userId);
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
            java.io.File dir = new java.io.File(System.getProperty("user.dir"), "uploads/chat");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String origName = file.getOriginalFilename();
            String ext = "";
            if (origName != null && origName.contains(".")) {
                ext = origName.substring(origName.lastIndexOf("."));
            } else {
                ext = ".png";
            }

            String savedName = UUID.randomUUID().toString() + ext;
            java.io.File targetFile = new java.io.File(dir, savedName);
            file.transferTo(targetFile.getAbsoluteFile());

            String fileUrl = "/api/chat/files/" + savedName;
            return ResponseEntity.ok(Map.of(
                    "fileUrl", fileUrl,
                    "fileName", origName != null ? origName : savedName,
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
            java.io.File targetFile = new java.io.File(System.getProperty("user.dir") + "/uploads/chat", fileName);
            if (!targetFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(targetFile.toURI());
            String contentType = Files.probeContentType(targetFile.toPath());
            if (contentType == null || contentType.equals("application/octet-stream")) {
                String lower = fileName.toLowerCase();
                if (lower.endsWith(".png")) contentType = "image/png";
                else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (lower.endsWith(".gif")) contentType = "image/gif";
                else if (lower.endsWith(".webp")) contentType = "image/webp";
                else contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve chat file: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
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
