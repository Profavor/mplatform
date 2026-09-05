package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationChannelResponse;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import com.classification.domain_system.integration.JdbcDynamicExecutionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IntegrationChannelServiceTest {

    private IntegrationChannelService service;
    private IntegrationChannelRepository repository;
    private com.classification.domain_system.repository.IntegrationLogRepository logRepository;
    private FieldEncryptionService encryptionService;
    private JdbcDynamicExecutionService jdbcService;

    @BeforeEach
    void setUp() {
        repository = mock(IntegrationChannelRepository.class);
        logRepository = mock(com.classification.domain_system.repository.IntegrationLogRepository.class);
        encryptionService = mock(FieldEncryptionService.class);
        jdbcService = mock(JdbcDynamicExecutionService.class);
        service = new IntegrationChannelService(repository, logRepository, encryptionService, jdbcService);
    }

    @Test
    void createChannel_EncryptsPasswordAndReturnsMasked() {
        // Arrange
        IntegrationChannel input = new IntegrationChannel();
        input.setConfigJson("{\"url\":\"jdbc:test\",\"password\":\"mySecret\"}");
        
        when(encryptionService.isEncrypted("mySecret")).thenReturn(false);
        when(encryptionService.encrypt("mySecret")).thenReturn("encryptedSecret");
        
        IntegrationChannel saved = new IntegrationChannel();
        saved.setId(UUID.randomUUID());
        saved.setConfigJson("{\"url\":\"jdbc:test\",\"password\":\"encryptedSecret\"}");
        when(repository.save(any(IntegrationChannel.class))).thenReturn(saved);

        // Act
        IntegrationChannelResponse response = service.createChannel(input);

        // Assert
        ArgumentCaptor<IntegrationChannel> captor = ArgumentCaptor.forClass(IntegrationChannel.class);
        verify(repository).save(captor.capture());
        
        String savedConfig = captor.getValue().getConfigJson();
        assertTrue(savedConfig.contains("encryptedSecret"));
        assertFalse(savedConfig.contains("mySecret"));
        
        assertTrue(response.getConfigJson().contains("********"));
        assertFalse(response.getConfigJson().contains("encryptedSecret"));
    }

    @Test
    void updateChannel_WithMaskedPassword_KeepsOldPassword() {
        // Arrange
        UUID id = UUID.randomUUID();
        
        IntegrationChannel existing = new IntegrationChannel();
        existing.setId(id);
        existing.setConfigJson("{\"url\":\"jdbc:test\",\"password\":\"oldEncryptedSecret\"}");
        
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        
        IntegrationChannel updated = new IntegrationChannel();
        updated.setConfigJson("{\"url\":\"jdbc:test\",\"password\":\"********\"}");
        
        when(repository.save(any(IntegrationChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<IntegrationChannelResponse> responseOpt = service.updateChannel(id, updated);

        // Assert
        assertTrue(responseOpt.isPresent());
        ArgumentCaptor<IntegrationChannel> captor = ArgumentCaptor.forClass(IntegrationChannel.class);
        verify(repository).save(captor.capture());
        
        String savedConfig = captor.getValue().getConfigJson();
        assertTrue(savedConfig.contains("oldEncryptedSecret"));
        
        assertTrue(responseOpt.get().getConfigJson().contains("********"));
    }

    @Test
    void createChannel_EncryptsHeadersAndReturnsMasked() {
        // Arrange
        IntegrationChannel input = new IntegrationChannel();
        input.setType("WEB_SERVICE");
        input.setDirection("OUTBOUND");
        input.setConfigJson("{\"url\":\"https://cartbom.com/api/partners/ingest\",\"method\":\"POST\",\"headers\":[{\"key\":\"x-cartbom-source-token\",\"value\":\"90be15c05c0bcadc\"}]}");

        when(encryptionService.isEncrypted("90be15c05c0bcadc")).thenReturn(false);
        when(encryptionService.encrypt("90be15c05c0bcadc")).thenReturn("ENC(token123)");

        IntegrationChannel saved = new IntegrationChannel();
        saved.setId(UUID.randomUUID());
        saved.setConfigJson("{\"url\":\"https://cartbom.com/api/partners/ingest\",\"method\":\"POST\",\"headers\":[{\"key\":\"x-cartbom-source-token\",\"value\":\"ENC(token123)\"}]}");
        when(repository.save(any(IntegrationChannel.class))).thenReturn(saved);

        // Act
        IntegrationChannelResponse response = service.createChannel(input);

        // Assert
        ArgumentCaptor<IntegrationChannel> captor = ArgumentCaptor.forClass(IntegrationChannel.class);
        verify(repository).save(captor.capture());

        String savedConfig = captor.getValue().getConfigJson();
        assertTrue(savedConfig.contains("ENC(token123)"));
        assertFalse(savedConfig.contains("90be15c05c0bcadc"));

        // When masked response is returned, the header value should be masked (e.g. 90be******** or ********)
        assertTrue(response.getConfigJson().contains("********"));
        assertFalse(response.getConfigJson().contains("90be15c05c0bcadc"));
    }

    @Test
    void updateChannel_WithMaskedHeader_KeepsOldEncryptedHeader() {
        // Arrange
        UUID id = UUID.randomUUID();

        IntegrationChannel existing = new IntegrationChannel();
        existing.setId(id);
        existing.setConfigJson("{\"url\":\"https://cartbom.com/api/partners/ingest\",\"method\":\"POST\",\"headers\":[{\"key\":\"x-cartbom-source-token\",\"value\":\"ENC(oldSecret123)\"}]}");

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        IntegrationChannel updated = new IntegrationChannel();
        updated.setConfigJson("{\"url\":\"https://cartbom.com/api/partners/ingest\",\"method\":\"POST\",\"headers\":[{\"key\":\"x-cartbom-source-token\",\"value\":\"90be********\"}]}");

        when(repository.save(any(IntegrationChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<IntegrationChannelResponse> responseOpt = service.updateChannel(id, updated);

        // Assert
        assertTrue(responseOpt.isPresent());
        ArgumentCaptor<IntegrationChannel> captor = ArgumentCaptor.forClass(IntegrationChannel.class);
        verify(repository).save(captor.capture());

        String savedConfig = captor.getValue().getConfigJson();
        assertTrue(savedConfig.contains("ENC(oldSecret123)"));
        assertFalse(savedConfig.contains("90be********"));
    }

    @Test
    void createChannel_GeneratesChannelCodeIfMissing() {
        // Arrange
        IntegrationChannel input = new IntegrationChannel();
        input.setName("Test Channel");
        input.setType("WEB_SERVICE");
        // channelCode is null

        when(repository.save(any(IntegrationChannel.class))).thenAnswer(invocation -> {
            IntegrationChannel ch = invocation.getArgument(0);
            ch.setId(UUID.randomUUID());
            return ch;
        });

        // Act
        IntegrationChannelResponse response = service.createChannel(input);

        // Assert
        assertNotNull(response.getChannelCode());
        assertTrue(response.getChannelCode().startsWith("CH-"));
    }

    @Test
    void updateChannel_UpdatesChannelCodeWhenProvided() {
        // Arrange
        UUID id = UUID.randomUUID();
        IntegrationChannel existing = new IntegrationChannel();
        existing.setId(id);
        existing.setName("Existing");
        existing.setChannelCode("CH-OLD-001");

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        IntegrationChannel updated = new IntegrationChannel();
        updated.setName("Updated");
        updated.setChannelCode("CH-NEW-002");

        when(repository.save(any(IntegrationChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<IntegrationChannelResponse> responseOpt = service.updateChannel(id, updated);

        // Assert
        assertTrue(responseOpt.isPresent());
        assertEquals("CH-NEW-002", responseOpt.get().getChannelCode());
    }
}
