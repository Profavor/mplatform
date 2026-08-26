package com.classification.domain_system.service.mail;

import com.classification.domain_system.dto.MailingListRequest;
import com.classification.domain_system.dto.MailingListResponse;
import com.classification.domain_system.entity.MailingList;
import com.classification.domain_system.entity.MailingListMember;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.MailingListMemberRepository;
import com.classification.domain_system.repository.MailingListRepository;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingListService {

    private final MailingListRepository mailingListRepository;
    private final MailingListMemberRepository mailingListMemberRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${mail.config-path:${mail.postfix.config-path:/var/mail-config}}")
    private String postfixConfigPath;

    @Transactional
    public MailingListResponse createMailingList(MailingListRequest request, String createdBy) {
        if (mailingListRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Mailing list with email already exists: " + request.getEmail());
        }

        MailingList list = new MailingList();
        list.setName(request.getName());
        list.setEmail(request.getEmail());
        list.setCreatedBy(createdBy);
        
        if (request.getDescription() != null) {
            try {
                list.setDescription(objectMapper.writeValueAsString(request.getDescription()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize description", e);
            }
        }

        processMembers(list, request.getMemberUserIds(), request.getMemberExternalEmails());

        mailingListRepository.save(list);

        syncPostfixAliases();
        return toResponse(list);
    }

    @Transactional
    public MailingListResponse updateMailingList(UUID id, MailingListRequest request) {
        MailingList list = mailingListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mailing list not found"));
                
        list.setName(request.getName());
        
        if (!list.getEmail().equals(request.getEmail()) && mailingListRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Mailing list with email already exists: " + request.getEmail());
        }
        list.setEmail(request.getEmail());
        
        if (request.getDescription() != null) {
            try {
                list.setDescription(objectMapper.writeValueAsString(request.getDescription()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize description", e);
            }
        } else {
            list.setDescription(null);
        }
        
        // Remove old members and add new ones
        list.getMembers().clear();
        processMembers(list, request.getMemberUserIds(), request.getMemberExternalEmails());
        
        mailingListRepository.save(list);
        syncPostfixAliases();
        
        return toResponse(list);
    }

    private void processMembers(MailingList list, List<String> userIds, List<String> externalEmails) {
        if (userIds != null) {
            for (String uid : userIds) {
                MailingListMember member = new MailingListMember();
                member.setUserId(uid);
                list.addMember(member);
            }
        }
        if (externalEmails != null) {
            for (String ext : externalEmails) {
                MailingListMember member = new MailingListMember();
                member.setExternalEmail(ext);
                list.addMember(member);
            }
        }
    }

    @Transactional
    public void deleteMailingList(UUID id) {
        MailingList list = mailingListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mailing list not found"));
        list.setIsActive(false);
        mailingListRepository.save(list);
        syncPostfixAliases();
    }

    @Transactional(readOnly = true)
    public Page<MailingListResponse> getMailingLists(Pageable pageable) {
        return mailingListRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MailingListResponse getMailingList(UUID id) {
        MailingList list = mailingListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mailing list not found"));
        return toResponse(list);
    }

    @Transactional
    public MailingListResponse addMember(UUID listId, String memberId) {
        MailingList list = mailingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Mailing list not found"));
                
        MailingListMember member = new MailingListMember();
        if (memberId.contains("@")) {
            member.setExternalEmail(memberId);
        } else {
            member.setUserId(memberId);
        }
        
        list.addMember(member);
        mailingListRepository.save(list);
        syncPostfixAliases();
        
        return toResponse(list);
    }

    @Transactional
    public void removeMember(UUID listId, UUID memberId) {
        MailingList list = mailingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Mailing list not found"));
                
        list.getMembers().removeIf(m -> m.getId().equals(memberId));
        mailingListRepository.save(list);
        syncPostfixAliases();
    }

    @Transactional(readOnly = true)
    public void syncPostfixAliases() {
        List<MailingList> activeLists = mailingListRepository.findByIsActiveTrue();
        
        File dir = new File(postfixConfigPath);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Could not create postfix directory: {}", postfixConfigPath);
            // In a real environment, this might throw or silently fail.
            // We proceed if we can.
        }
        
        File aliasFile = new File(dir, "postfix-virtual.cf");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aliasFile))) {
            for (MailingList list : activeLists) {
                List<String> memberEmails = new ArrayList<>();
                for (MailingListMember m : list.getMembers()) {
                    if (StringUtils.hasText(m.getExternalEmail())) {
                        memberEmails.add(m.getExternalEmail());
                    } else if (StringUtils.hasText(m.getUserId())) {
                        userRepository.findById(m.getUserId()).ifPresent(u -> {
                            if (StringUtils.hasText(u.getEmail())) {
                                memberEmails.add(u.getEmail());
                            }
                        });
                    }
                }
                
                if (!memberEmails.isEmpty()) {
                    writer.write(list.getEmail() + " " + String.join(", ", memberEmails));
                    writer.newLine();
                }
            }
            log.info("Successfully synced postfix aliases to {}", aliasFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write postfix aliases", e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> resolveMailingListMembers(String email) {
        return mailingListRepository.findByEmail(email)
                .filter(MailingList::getIsActive)
                .map(list -> {
                    List<String> emails = new ArrayList<>();
                    for (MailingListMember m : list.getMembers()) {
                        if (StringUtils.hasText(m.getExternalEmail())) {
                            emails.add(m.getExternalEmail());
                        } else if (StringUtils.hasText(m.getUserId())) {
                            userRepository.findById(m.getUserId()).ifPresent(u -> {
                                if (StringUtils.hasText(u.getEmail())) {
                                    emails.add(u.getEmail());
                                }
                            });
                        }
                    }
                    return emails;
                })
                .orElse(Collections.emptyList());
    }
    
    private MailingListResponse toResponse(MailingList list) {
        MailingListResponse res = new MailingListResponse();
        res.setId(list.getId());
        res.setName(list.getName());
        res.setEmail(list.getEmail());
        res.setActive(list.getIsActive() != null ? list.getIsActive() : true);
        res.setCreatedAt(list.getCreatedAt());
        
        if (StringUtils.hasText(list.getDescription())) {
            try {
                res.setDescription(objectMapper.readValue(list.getDescription(), new TypeReference<Map<String, String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse description", e);
            }
        }
        
        if (list.getMembers() != null) {
            res.setMemberCount(list.getMembers().size());
            List<MailingListResponse.MemberInfo> members = list.getMembers().stream().map(m -> {
                MailingListResponse.MemberInfo info = new MailingListResponse.MemberInfo();
                info.setId(m.getId());
                info.setUserId(m.getUserId());
                if (m.getUserId() != null) {
                    userRepository.findById(m.getUserId()).ifPresent(u -> info.setUserName(u.getUsername()));
                }
                info.setExternalEmail(m.getExternalEmail());
                return info;
            }).collect(Collectors.toList());
            res.setMembers(members);
        } else {
            res.setMemberCount(0);
            res.setMembers(Collections.emptyList());
        }
        
        return res;
    }
}
