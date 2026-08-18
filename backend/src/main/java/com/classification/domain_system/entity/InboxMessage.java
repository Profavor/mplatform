package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "inbox_message")
@Getter
@Setter
@NoArgsConstructor
public class InboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_id", length = 100)
    private String senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User sender;

    @Transient
    private String senderName;

    public String getSenderName() {
        if (sender != null && sender.getUsername() != null) {
            return sender.getUsername();
        }
        return senderName != null ? senderName : senderId;
    }

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "importance", length = 20)
    private String importance = "NORMAL";

    @Column(name = "message_type", nullable = false, length = 30)
    private String messageType = "INTERNAL";

    @Column(name = "parent_message_id")
    private UUID parentMessageId;

    @Column(name = "root_message_id")
    private UUID rootMessageId;

    @Column(name = "related_approval_id")
    private UUID relatedApprovalId;

    @Column(name = "external_message_id", unique = true)
    private String externalMessageId;

    @Column(name = "is_draft")
    private Boolean isDraft = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(columnDefinition = "bigint default 0")
    private Long version = 0L;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboxRecipient> recipients = new ArrayList<>();

    public void addRecipient(InboxRecipient recipient) {
        if (recipient != null) {
            this.recipients.add(recipient);
            recipient.setMessage(this);
        }
    }

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboxAttachment> attachments = new ArrayList<>();

    public void addAttachment(InboxAttachment attachment) {
        if (attachment != null) {
            this.attachments.add(attachment);
            attachment.setMessage(this);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
