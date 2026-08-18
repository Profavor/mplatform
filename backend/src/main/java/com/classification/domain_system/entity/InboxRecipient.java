package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbox_recipient", indexes = {
    @Index(name = "idx_inbox_recipient_user_folder_deleted", columnList = "user_id, folder, is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
public class InboxRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private InboxMessage message;

    @Column(name = "user_id", length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Transient
    private String recipientName;

    public String getRecipientName() {
        if (user != null && user.getUsername() != null) {
            return user.getUsername();
        }
        return recipientName != null ? recipientName : userId;
    }

    @Column(name = "email")
    private String email;

    @Column(name = "recipient_type", nullable = false, length = 10)
    private String recipientType;

    @Column(name = "folder", nullable = false, length = 20)
    private String folder;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_starred")
    private Boolean isStarred = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "is_recalled")
    private Boolean isRecalled = false;

    @Column(name = "recalled_at")
    private LocalDateTime recalledAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
