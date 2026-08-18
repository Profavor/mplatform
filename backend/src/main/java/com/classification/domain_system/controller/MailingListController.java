package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MailingListRequest;
import com.classification.domain_system.service.mail.MailingListService;
import com.classification.domain_system.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/mailing-lists")
@RequiredArgsConstructor
@PreAuthorize("hasPermission(null, 'admin:write')")
public class MailingListController {

    private final MailingListService mailingListService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<?> list(Pageable pageable) {
        return ResponseEntity.ok(mailingListService.getMailingLists(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mailingListService.getMailingList(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody MailingListRequest request) {
        return ResponseEntity.ok(mailingListService.createMailingList(request, securityUtils.getCurrentUserIdOrThrow()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody MailingListRequest request) {
        return ResponseEntity.ok(mailingListService.updateMailingList(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        mailingListService.deleteMailingList(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        mailingListService.addMember(id, body.get("email"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable UUID id, @PathVariable UUID memberId) {
        mailingListService.removeMember(id, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync-aliases")
    public ResponseEntity<?> syncAliases() {
        mailingListService.syncPostfixAliases();
        return ResponseEntity.ok().build();
    }
}
