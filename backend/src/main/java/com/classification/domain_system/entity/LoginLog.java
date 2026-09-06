package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @CreationTimestamp
    @Column(name = "login_at", updatable = false)
    private LocalDateTime loginAt;

    @Column(name = "two_factor_status", length = 30)
    private String twoFactorStatus;

    @Column(name = "two_factor_type", length = 20)
    private String twoFactorType;
}
