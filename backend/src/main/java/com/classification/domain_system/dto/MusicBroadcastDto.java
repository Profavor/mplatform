package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicBroadcastDto {

    private String videoId;
    private String title;
    private Boolean isPlaying;
    private Double seekSeconds;
    private LocalDateTime updatedAt;
    private String djName;
    private String playlistTitle;
}
