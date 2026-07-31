package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_youtube_config")
@Getter
@Setter
@NoArgsConstructor
public class UserYoutubeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String youtubeChannelUrl;

    @Column(columnDefinition = "TEXT")
    private String playlistId;

    @Column(columnDefinition = "TEXT")
    private String playlistTitle;

    @Column(columnDefinition = "TEXT")
    private String apiKey;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
