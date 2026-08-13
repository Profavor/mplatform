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
    private FieldEncryptionService encryptionService;
    private JdbcDynamicExecutionService jdbcService;

    @BeforeEach
    void setUp() {
        repository = mock(IntegrationChannelRepository.class);
        encryptionService = mock(FieldEncryptionService.class);
        jdbcService = mock(JdbcDynamicExecutionService.class);
        service = new IntegrationChannelService(repository, encryptionService, jdbcService);
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
}
