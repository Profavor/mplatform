package com.classification.domain_system.controller;

import com.classification.domain_system.service.mail.MailAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/mail")
@RequiredArgsConstructor
@PreAuthorize("hasPermission(null, 'admin:write')")
public class MailAccountController {

    private final MailAccountService mailAccountService;

    @GetMapping("/accounts")
    public ResponseEntity<?> listAccounts() throws IOException {
        return ResponseEntity.ok(mailAccountService.listAccounts());
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> body) throws IOException {
        mailAccountService.createAccount(body.get("email"), body.get("password"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/accounts/{email}")
    public ResponseEntity<?> deleteAccount(@PathVariable String email) throws IOException {
        mailAccountService.deleteAccount(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accounts/{email}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String email, @RequestBody Map<String, String> body) throws IOException {
        mailAccountService.updatePassword(email, body.get("password"));
        return ResponseEntity.ok().build();
    }
}
