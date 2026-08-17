package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.dto.DomainRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DomainService {
    
    private final DomainRepository domainRepository;
    private final com.classification.domain_system.repository.UserRepository userRepository;
    private final com.classification.domain_system.security.SecurityUtils securityUtils;
    
    @Transactional
    public Domain createDomain(DomainRequest request) {
        Domain domain = new Domain();
        domain.setName(request.getName());
        domain.setDescription(request.getDescription());
        domain.setIdentifierFieldId(request.getIdentifierFieldId());
        domain.setDisplayNameFieldId(request.getDisplayNameFieldId());
        domain.setDescriptionFieldId(request.getDescriptionFieldId());
        domain.setImageFieldId(request.getImageFieldId());
        domain.setIcon(request.getIcon());
        domain.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        domain.setNumberingPattern(request.getNumberingPattern());
        domain.setAutoDqScanEnabled(request.getAutoDqScanEnabled() != null ? request.getAutoDqScanEnabled() : false);
        return domainRepository.save(domain);
    }
    
    @Transactional(readOnly = true)
    public List<Domain> getAllDomains() {
        String username = securityUtils.getCurrentUserId();
        if (username == null) {
            return java.util.Collections.emptyList();
        }
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        boolean hasFullAccess = auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> "*:*".equals(a.getAuthority()) 
                            || "*".equals(a.getAuthority()) 
                            || "domain:*".equalsIgnoreCase(a.getAuthority())
                            || "admin:read".equalsIgnoreCase(a.getAuthority())
                            || "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority())
                            || "ROLE_SYSTEM_ADMIN".equalsIgnoreCase(a.getAuthority())
                            || "ROLE_SUPERADMIN".equalsIgnoreCase(a.getAuthority())
                            || "ROLE_SYSTEM".equalsIgnoreCase(a.getAuthority()));

        if (hasFullAccess) {
            return domainRepository.findAllByOrderBySortOrderAsc();
        }

        com.classification.domain_system.entity.User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        
        return domainRepository.findAllByUserIdOrderBySortOrderAsc(user.getId());
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "domains", key = "#id")
    public Domain getDomainById(UUID id) {
        return domainRepository.findById(id).orElseThrow(() -> new RuntimeException("Domain not found"));
    }

    @Transactional(readOnly = true)
    public Domain getDomain(UUID id) {
        return getDomainById(id);
    }
    
    @Transactional
    @CacheEvict(value = "domains", key = "#id")
    public Domain updateDomain(UUID id, DomainRequest request) {
        Domain domain = getDomain(id);
        if (request.getName() != null && !request.getName().isEmpty()) {
            domain.setName(request.getName());
        }
        if (request.getDescription() != null) {
            domain.setDescription(request.getDescription());
        }
        domain.setIdentifierFieldId(request.getIdentifierFieldId());
        domain.setDisplayNameFieldId(request.getDisplayNameFieldId());
        domain.setDescriptionFieldId(request.getDescriptionFieldId());
        domain.setImageFieldId(request.getImageFieldId());
        domain.setIcon(request.getIcon());
        domain.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : domain.getSortOrder());
        if (request.getNumberingPattern() != null) {
            domain.setNumberingPattern(request.getNumberingPattern());
        }
        if (request.getAutoDqScanEnabled() != null) {
            domain.setAutoDqScanEnabled(request.getAutoDqScanEnabled());
        }
        return domainRepository.save(domain);
    }

    @Transactional
    @CacheEvict(value = "domains", key = "#id")
    public void deleteDomain(UUID id) {
        domainRepository.deleteById(id);
    }
}
