package com.classification.domain_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailingListRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    private Map<String, String> description;
    private List<String> memberUserIds;
    private List<String> memberExternalEmails;
}
