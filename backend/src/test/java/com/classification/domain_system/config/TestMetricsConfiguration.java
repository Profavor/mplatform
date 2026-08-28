package com.classification.domain_system.config;

import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestMetricsConfiguration {

    @Bean
    @Primary
    public ProcessorMetrics processorMetrics() {
        return Mockito.mock(ProcessorMetrics.class);
    }

    @Bean
    @Primary
    public FileDescriptorMetrics fileDescriptorMetrics() {
        return Mockito.mock(FileDescriptorMetrics.class);
    }

    @Bean
    @Primary
    public DiskSpaceMetrics diskSpaceMetrics() {
        return Mockito.mock(DiskSpaceMetrics.class);
    }

    @Bean
    @Primary
    public UptimeMetrics uptimeMetrics() {
        return Mockito.mock(UptimeMetrics.class);
    }
}
