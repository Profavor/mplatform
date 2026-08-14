package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MusicBroadcastDto;
import com.classification.domain_system.entity.UserYoutubeConfig;
import com.classification.domain_system.service.MusicBroadcastService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicBroadcastControllerTest {

    @Mock
    private MusicBroadcastService musicBroadcastService;

    @InjectMocks
    private MusicBroadcastController musicBroadcastController;

    @Test
    @DisplayName("음악 방송 상태 조회 성공")
    void testGetCurrentState_Success() {
        MusicBroadcastDto dto = MusicBroadcastDto.builder()
                .isPlaying(true)
                .title("Test Song")
                .build();

        when(musicBroadcastService.getCurrentState()).thenReturn(dto);

        ResponseEntity<MusicBroadcastDto> response = musicBroadcastController.getCurrentState();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getIsPlaying());
        assertEquals("Test Song", response.getBody().getTitle());
    }

    @Test
    @DisplayName("유튜브 설정 조회 성공")
    void testGetYoutubeConfig_Success() {
        UserYoutubeConfig config = new UserYoutubeConfig();
        config.setPlaylistId("PL12345");

        when(musicBroadcastService.getYoutubeConfig(any())).thenReturn(config);

        ResponseEntity<UserYoutubeConfig> response = musicBroadcastController.getYoutubeConfig();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PL12345", response.getBody().getPlaylistId());
    }

    @Test
    @DisplayName("유튜브 설정 저장 성공")
    void testSaveYoutubeConfig_Success() {
        MusicBroadcastController.YoutubeConfigRequest req = new MusicBroadcastController.YoutubeConfigRequest();
        req.setPlaylistId("PL12345");
        req.setPlaylistTitle("My List");

        UserYoutubeConfig config = new UserYoutubeConfig();
        config.setPlaylistId("PL12345");

        when(musicBroadcastService.saveYoutubeConfig(any(), any(), eq("PL12345"), eq("My List"), any()))
                .thenReturn(config);

        ResponseEntity<UserYoutubeConfig> response = musicBroadcastController.saveYoutubeConfig(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PL12345", response.getBody().getPlaylistId());
    }

    @Test
    @DisplayName("음악 재생 및 정지 제어 성공")
    void testPlayAndStopMusic_Success() {
        MusicBroadcastController.PlayRequest req = new MusicBroadcastController.PlayRequest();
        req.setVideoId("vid123");
        req.setTitle("Cool Song");
        req.setSeekSeconds(0.0);
        req.setPlaylistTitle("Hits");

        MusicBroadcastDto playDto = MusicBroadcastDto.builder()
                .isPlaying(true)
                .title("Cool Song")
                .build();

        when(musicBroadcastService.playMusic(eq("vid123"), eq("Cool Song"), eq(0.0), any(), eq("Hits")))
                .thenReturn(playDto);

        ResponseEntity<MusicBroadcastDto> playResponse = musicBroadcastController.playMusic(req);
        assertEquals(HttpStatus.OK, playResponse.getStatusCode());
        assertTrue(playResponse.getBody().getIsPlaying());

        MusicBroadcastDto stopDto = MusicBroadcastDto.builder()
                .isPlaying(false)
                .build();
        when(musicBroadcastService.stopMusic(any())).thenReturn(stopDto);

        ResponseEntity<MusicBroadcastDto> stopResponse = musicBroadcastController.stopMusic();
        assertEquals(HttpStatus.OK, stopResponse.getStatusCode());
        assertFalse(stopResponse.getBody().getIsPlaying());
    }
}
