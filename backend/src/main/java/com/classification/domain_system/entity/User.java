package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @UuidGenerator
    private String id;
    
    private String username;
    private String email;
    private String password;
    private String role;
    private String timezone;

    @Column(name = "organization_id")
    private UUID organizationId;
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;

    @Column(name = "department_id")
    private UUID departmentId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @Column(name = "team_id")
    private UUID teamId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    private Boolean isActive = true;

    @Column(name = "active_session_id")
    private String activeSessionId;

    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;

    @Column(name = "encrypted_temp_password")
    private String encryptedTempPassword;

    @Column(name = "failed_login_count")
    private Integer failedLoginCount = 0;

    @Column(name = "locked_until")
    private java.time.LocalDateTime lockedUntil;
}
