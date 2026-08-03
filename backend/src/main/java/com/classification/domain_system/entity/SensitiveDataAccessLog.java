package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensitive_data_access_log")
@Getter
@Setter
@NoArgsConstructor
public class SensitiveDataAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "field_keys", length = 500)
    private String fieldKeys;

    @Column(name = "access_reason", length = 500)
    private String accessReason;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    @PrePersist
    protected void onCreate() {
        if (this.accessedAt == null) {
            this.accessedAt = LocalDateTime.now();
        }
    }
}
