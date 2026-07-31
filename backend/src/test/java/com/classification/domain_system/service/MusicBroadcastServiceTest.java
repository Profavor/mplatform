package com.classification.domain_system.service;

import com.classification.domain_system.dto.MusicBroadcastDto;
import com.classification.domain_system.entity.UserYoutubeConfig;
import com.classification.domain_system.repository.UserYoutubeConfigRepository;
import com.classification.domain_system.websocket.WebSocketPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MusicBroadcastServiceTest {

    @Mock
    private UserYoutubeConfigRepository configRepository;

    @Mock
    private WebSocketPublisher webSocketPublisher;

    @Mock
    private SseNotificationService sseNotificationService;

    @InjectMocks
    private MusicBroadcastService musicBroadcastService;

    @Test
    @DisplayName("관리자 DJ 라이브 라디오 방송 시작 및 상태 동기화 검증")
    void playMusic_Success() {
        // given
        String videoId = "5qap5aO4i9A";
        String title = "Lofi Hip Hop Radio";
        String djName = "admin_dj";

        // when
        MusicBroadcastDto dto = musicBroadcastService.playMusic(videoId, title, 10.0, djName, "My Playlist");

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getIsPlaying()).isTrue();
        assertThat(dto.getVideoId()).isEqualTo(videoId);
        assertThat(dto.getDjName()).isEqualTo(djName);

        // state sync check
        MusicBroadcastDto currentState = musicBroadcastService.getCurrentState();
        assertThat(currentState.getIsPlaying()).isTrue();
        assertThat(currentState.getVideoId()).isEqualTo(videoId);
    }

    @Test
    @DisplayName("관리자 DJ 라디오 방송 중단 검증")
    void stopMusic_Success() {
        // given
        musicBroadcastService.playMusic("5qap5aO4i9A", "Lofi Radio", 0.0, "admin_dj", null);

        // when
        MusicBroadcastDto stopped = musicBroadcastService.stopMusic("admin_dj");

        // then
        assertThat(stopped.getIsPlaying()).isFalse();

        MusicBroadcastDto currentState = musicBroadcastService.getCurrentState();
        assertThat(currentState.getIsPlaying()).isFalse();
    }

    @Test
    @DisplayName("관리자 유튜브 계정 및 플레이리스트 설정 저장 검증")
    void saveYoutubeConfig_Success() {
        // given
        String userId = "admin_user";
        UserYoutubeConfig mockConfig = new UserYoutubeConfig();
        mockConfig.setUserId(userId);
        mockConfig.setPlaylistTitle("내 유튜브 뮤직");

        given(configRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(configRepository.save(any(UserYoutubeConfig.class))).willReturn(mockConfig);

        // when
        UserYoutubeConfig saved = musicBroadcastService.saveYoutubeConfig(userId, "https://youtube.com/@channel", "PL12345", "내 유튜브 뮤직", null);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPlaylistTitle()).isEqualTo("내 유튜브 뮤직");
    }
}
