package com.classification.domain_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Data
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", length = 255)
    private String configValue;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.updatedAt = LocalDateTime.now();
    }
}
