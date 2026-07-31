package com.classification.domain_system.service;

import com.classification.domain_system.dto.MusicBroadcastDto;
import com.classification.domain_system.entity.UserYoutubeConfig;
import com.classification.domain_system.repository.UserYoutubeConfigRepository;
import com.classification.domain_system.websocket.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicBroadcastService {

    private final UserYoutubeConfigRepository configRepository;
    private final WebSocketPublisher webSocketPublisher;
    private final SseNotificationService sseNotificationService;

    // 현재 라이브 라디오 방송 상태 메모리 보관
    private final Map<String, Object> currentBroadcastState = new ConcurrentHashMap<>();

    @Transactional
    public UserYoutubeConfig saveYoutubeConfig(String userId, String channelUrl, String playlistId, String playlistTitle, String apiKey) {
        UserYoutubeConfig config = configRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserYoutubeConfig newConfig = new UserYoutubeConfig();
                    newConfig.setUserId(userId);
                    return newConfig;
                });

        if (channelUrl != null) config.setYoutubeChannelUrl(channelUrl.trim());
        if (playlistId != null) config.setPlaylistId(playlistId.trim());
        if (playlistTitle != null) config.setPlaylistTitle(playlistTitle.trim());
        if (apiKey != null) config.setApiKey(apiKey.trim());

        return configRepository.save(config);
    }

    @Transactional(readOnly = true)
    public UserYoutubeConfig getYoutubeConfig(String userId) {
        return configRepository.findByUserId(userId).orElse(null);
    }

    public MusicBroadcastDto playMusic(String videoId, String title, Double seekSeconds, String djName, String playlistTitle) {
        LocalDateTime now = LocalDateTime.now();
        double seek = seekSeconds != null ? seekSeconds : 0.0;

        MusicBroadcastDto dto = MusicBroadcastDto.builder()
                .videoId(videoId)
                .title(title != null && !title.trim().isEmpty() ? title : "Live Radio Track")
                .isPlaying(true)
                .seekSeconds(seek)
                .updatedAt(now)
                .djName(djName != null ? djName : "관리자 DJ")
                .playlistTitle(playlistTitle)
                .build();

        currentBroadcastState.put("videoId", dto.getVideoId());
        currentBroadcastState.put("title", dto.getTitle());
        currentBroadcastState.put("isPlaying", true);
        currentBroadcastState.put("seekSeconds", dto.getSeekSeconds());
        currentBroadcastState.put("updatedAt", now);
        currentBroadcastState.put("djName", dto.getDjName());
        if (playlistTitle != null) currentBroadcastState.put("playlistTitle", playlistTitle);

        Map<String, Object> event = Map.of(
                "eventType", "MUSIC_PLAY",
                "videoId", dto.getVideoId(),
                "title", dto.getTitle(),
                "isPlaying", true,
                "seekSeconds", dto.getSeekSeconds(),
                "updatedAt", now.toString(),
                "djName", dto.getDjName()
        );

        broadcastEvent(event);
        log.info("[MUSIC_BROADCAST] DJ {} started playing track: {} (VideoId: {})", dto.getDjName(), dto.getTitle(), dto.getVideoId());
        return dto;
    }

    public MusicBroadcastDto stopMusic(String djName) {
        currentBroadcastState.put("isPlaying", false);
        LocalDateTime now = LocalDateTime.now();

        MusicBroadcastDto dto = MusicBroadcastDto.builder()
                .videoId((String) currentBroadcastState.getOrDefault("videoId", ""))
                .title((String) currentBroadcastState.getOrDefault("title", ""))
                .isPlaying(false)
                .seekSeconds(0.0)
                .updatedAt(now)
                .djName(djName != null ? djName : "관리자 DJ")
                .build();

        Map<String, Object> event = Map.of(
                "eventType", "MUSIC_STOP",
                "isPlaying", false,
                "djName", dto.getDjName()
        );

        broadcastEvent(event);
        log.info("[MUSIC_BROADCAST] DJ {} stopped music broadcast.", dto.getDjName());
        return dto;
    }

    public MusicBroadcastDto getCurrentState() {
        Boolean isPlaying = (Boolean) currentBroadcastState.getOrDefault("isPlaying", false);
        if (!isPlaying) {
            return MusicBroadcastDto.builder()
                    .isPlaying(false)
                    .build();
        }

        String videoId = (String) currentBroadcastState.get("videoId");
        String title = (String) currentBroadcastState.get("title");
        Double seek = (Double) currentBroadcastState.getOrDefault("seekSeconds", 0.0);
        LocalDateTime updatedAt = (LocalDateTime) currentBroadcastState.get("updatedAt");
        String djName = (String) currentBroadcastState.getOrDefault("djName", "관리자 DJ");
        String playlistTitle = (String) currentBroadcastState.get("playlistTitle");

        double elapsedSeconds = 0.0;
        if (updatedAt != null) {
            elapsedSeconds = Duration.between(updatedAt, LocalDateTime.now()).getSeconds();
        }

        return MusicBroadcastDto.builder()
                .videoId(videoId)
                .title(title)
                .isPlaying(true)
                .seekSeconds(seek + elapsedSeconds)
                .updatedAt(updatedAt)
                .djName(djName)
                .playlistTitle(playlistTitle)
                .build();
    }

    private void broadcastEvent(Map<String, Object> event) {
        if (webSocketPublisher != null) {
            try {
                webSocketPublisher.publishNotification("ALL", event);
            } catch (Exception e) {
                log.error("Failed to broadcast music event via WebSocket", e);
            }
        }
        if (sseNotificationService != null) {
            try {
                sseNotificationService.broadcastNotification(event);
            } catch (Exception e) {
                log.error("Failed to broadcast music event via SSE", e);
            }
        }
    }
}
