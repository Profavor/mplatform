package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailingListResponse {
    private UUID id;
    private String name;
    private String email;
    private Map<String, String> description;
    private boolean isActive;
    private List<MemberInfo> members;
    private int memberCount;
    private LocalDateTime createdAt;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private UUID id;
        private String userId;
        private String userName;
        private String externalEmail;
    }
}
