package com.classification.domain_system.controller;

import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.security.SecurityUtils;
import com.classification.domain_system.service.InboxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;
    private final SecurityUtils securityUtils;

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestParam(defaultValue = "INBOX") String folder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inboxService.getMessages(userId, folder, pageable, keyword));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<?> getMessage(@PathVariable UUID id) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.getMessage(userId, id));
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody InboxMessageRequest request) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        if (request.isDraft()) {
            return ResponseEntity.ok(inboxService.saveDraft(request, userId));
        }
        return ResponseEntity.ok(inboxService.sendMessage(request, userId));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<?> updateDraft(@PathVariable UUID id, @Valid @RequestBody InboxMessageRequest request) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.updateDraft(id, request, userId));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean permanent) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        if (permanent) {
            inboxService.permanentDelete(userId, id);
        } else {
            inboxService.moveToTrash(userId, id);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/{id}/reply")
    public ResponseEntity<?> reply(@PathVariable UUID id, @Valid @RequestBody InboxMessageRequest request) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.replyMessage(userId, id, request));
    }

    @PostMapping("/messages/{id}/reply-all")
    public ResponseEntity<?> replyAll(@PathVariable UUID id, @Valid @RequestBody InboxMessageRequest request) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.replyAllMessage(userId, id, request));
    }

    @PostMapping("/messages/{id}/forward")
    public ResponseEntity<?> forward(@PathVariable UUID id, @Valid @RequestBody InboxMessageRequest request) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.forwardMessage(userId, id, request));
    }

    @PatchMapping("/messages/{id}/read")
    public ResponseEntity<?> toggleRead(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        boolean isRead = body.getOrDefault("isRead", true);
        if (isRead) {
            inboxService.markAsRead(userId, id);
        } else {
            inboxService.markAsUnread(userId, id);
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/messages/{id}/star")
    public ResponseEntity<?> toggleStar(@PathVariable UUID id) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        inboxService.toggleStar(userId, id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/messages/{id}/folder")
    public ResponseEntity<?> moveToFolder(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        inboxService.moveToFolder(userId, id, body.get("folder"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/bulk-action")
    public ResponseEntity<?> bulkAction(@RequestBody Map<String, Object> body) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        String action = (String) body.get("action");
        @SuppressWarnings("unchecked")
        List<String> messageIdStrs = (List<String>) body.get("messageIds");
        List<UUID> messageIds = messageIdStrs.stream().map(UUID::fromString).toList();

        switch (action) {
            case "MARK_READ" -> inboxService.bulkMarkAsRead(userId, messageIds);
            case "MOVE_TO_TRASH" -> inboxService.bulkMoveToTrash(userId, messageIds);
            case "MOVE_TO_ARCHIVE" -> messageIds.forEach(id -> inboxService.moveToFolder(userId, id, "ARCHIVE"));
            default -> throw new IllegalArgumentException("Unknown bulk action: " + action);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/folder-counts")
    public ResponseEntity<?> getFolderCounts() {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.getFolderCounts(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(Map.of("unreadCount", inboxService.getUnreadCount(userId)));
    }

    @GetMapping("/messages/{id}/thread")
    public ResponseEntity<?> getThread(@PathVariable UUID id) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.getThread(userId, id));
    }

    @PostMapping("/messages/{id}/recall")
    public ResponseEntity<?> recallMessage(@PathVariable UUID id) {
        String userId = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(inboxService.recallMessage(userId, id));
    }

    private static final byte[] TRANSPARENT_1X1_GIF = new byte[]{
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 1, 0, 1, 0,
            (byte) 0x80, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            0x21, (byte) 0xf9, 4, 1, 0, 0, 0, 0,
            0x2c, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 2, 0x44, 1, 0, 0x3b
    };

    /**
     * 이메일 수신 확인용 투명 1x1 GIF 이미지 트래킹 픽셀
     * 외부 수신자의 메일 클라이언트(Outlook, Gmail 등)가 이미지를 비인증 GET 요청으로 로드하므로,
     * SecurityConfig 필터체인에서 permitAll로 허용하여 오픈 여부를 기록하고 투명 GIF를 응답합니다.
     */
    @GetMapping(value = "/track/open/{recipientId}", produces = MediaType.IMAGE_GIF_VALUE)
    public ResponseEntity<byte[]> trackOpen(@PathVariable UUID recipientId) {
        try {
            inboxService.trackEmailOpen(recipientId);
        } catch (Exception e) {
            // Ignore tracking exceptions
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .body(TRANSPARENT_1X1_GIF);
    }
}
