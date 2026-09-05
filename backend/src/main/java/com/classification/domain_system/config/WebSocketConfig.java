package com.classification.domain_system.config;

import com.classification.domain_system.websocket.StompJwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtInterceptor stompJwtInterceptor;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] patterns;
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            patterns = java.util.Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toArray(String[]::new);
        } else {
            patterns = new String[]{"http://localhost:3000", "http://localhost:8080"};
        }

        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(patterns)
                .withSockJS();
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(patterns);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtInterceptor);
    }
}
