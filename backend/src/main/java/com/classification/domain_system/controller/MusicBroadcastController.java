package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MusicBroadcastDto;
import com.classification.domain_system.entity.UserYoutubeConfig;
import com.classification.domain_system.service.MusicBroadcastService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicBroadcastController {

    private final MusicBroadcastService musicBroadcastService;

    @GetMapping("/state")
    public ResponseEntity<MusicBroadcastDto> getCurrentState() {
        return ResponseEntity.ok(musicBroadcastService.getCurrentState());
    }

    @GetMapping("/admin/youtube-config")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'admin:write')")
    public ResponseEntity<UserYoutubeConfig> getYoutubeConfig() {
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(musicBroadcastService.getYoutubeConfig(userId));
    }

    @PostMapping("/admin/youtube-config")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<UserYoutubeConfig> saveYoutubeConfig(@RequestBody YoutubeConfigRequest req) {
        String userId = getAuthenticatedUserId();
        UserYoutubeConfig config = musicBroadcastService.saveYoutubeConfig(
                userId,
                req.getYoutubeChannelUrl(),
                req.getPlaylistId(),
                req.getPlaylistTitle(),
                req.getApiKey()
        );
        return ResponseEntity.ok(config);
    }

    @PostMapping("/admin/play")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<MusicBroadcastDto> playMusic(@RequestBody PlayRequest req) {
        String djName = getAuthenticatedUserId();
        MusicBroadcastDto dto = musicBroadcastService.playMusic(
                req.getVideoId(),
                req.getTitle(),
                req.getSeekSeconds(),
                djName,
                req.getPlaylistTitle()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/admin/stop")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<MusicBroadcastDto> stopMusic() {
        String djName = getAuthenticatedUserId();
        return ResponseEntity.ok(musicBroadcastService.stopMusic(djName));
    }

    private String getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    @Data
    public static class YoutubeConfigRequest {
        private String youtubeChannelUrl;
        private String playlistId;
        private String playlistTitle;
        private String apiKey;
    }

    @Data
    public static class PlayRequest {
        private String videoId;
        private String title;
        private Double seekSeconds;
        private String playlistTitle;
    }
}
