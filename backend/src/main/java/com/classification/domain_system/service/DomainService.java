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

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getDomainLayout(UUID domainId) {
        Domain domain = getDomain(domainId);
        java.util.Map<String, Object> config = domain.getDetailLayoutConfig();
        if (config != null && !config.isEmpty()) {
            // Backward compatibility: If legacy format without 'layouts', wrap it into layouts array
            if (!config.containsKey("layouts") && config.containsKey("widgets")) {
                java.util.Map<String, Object> wrapped = new java.util.HashMap<>(config);
                java.util.Map<String, Object> defaultLayout = new java.util.HashMap<>();
                defaultLayout.put("id", "layout_default");
                defaultLayout.put("name", "기본 레이아웃");
                defaultLayout.put("isDefault", true);
                defaultLayout.put("cols", config.getOrDefault("cols", 12));
                defaultLayout.put("rowHeight", config.getOrDefault("rowHeight", 42));
                defaultLayout.put("widgets", config.getOrDefault("widgets", new java.util.ArrayList<>()));
                defaultLayout.put("options", config.getOrDefault("options", new java.util.HashMap<>()));

                java.util.List<java.util.Map<String, Object>> layoutList = new java.util.ArrayList<>();
                layoutList.add(defaultLayout);
                wrapped.put("layouts", layoutList);
                wrapped.put("activeLayoutId", "layout_default");
                return wrapped;
            }
            return config;
        }
        return new java.util.HashMap<>();
    }

    @Transactional
    @CacheEvict(value = "domains", key = "#domainId")
    public java.util.Map<String, Object> saveDomainLayout(UUID domainId, com.classification.domain_system.dto.RecordLayoutDto layoutDto) {
        Domain domain = getDomain(domainId);
        java.util.Map<String, Object> configMap = new java.util.HashMap<>();

        if (layoutDto.getLayouts() != null && !layoutDto.getLayouts().isEmpty()) {
            configMap.put("layouts", layoutDto.getLayouts());
            configMap.put("activeLayoutId", layoutDto.getActiveLayoutId() != null ? layoutDto.getActiveLayoutId() : "layout_default");
            
            // Also maintain top-level cols/rowHeight/widgets for default layout fallback
            java.util.Map<String, Object> firstLayout = layoutDto.getLayouts().get(0);
            configMap.put("cols", firstLayout.getOrDefault("cols", 12));
            configMap.put("rowHeight", firstLayout.getOrDefault("rowHeight", 42));
            configMap.put("widgets", firstLayout.getOrDefault("widgets", new java.util.ArrayList<>()));
            configMap.put("options", firstLayout.getOrDefault("options", new java.util.HashMap<>()));
        } else {
            java.util.Map<String, Object> defaultLayout = new java.util.HashMap<>();
            defaultLayout.put("id", "layout_default");
            defaultLayout.put("name", "기본 레이아웃");
            defaultLayout.put("isDefault", true);
            defaultLayout.put("cols", layoutDto.getCols() != null ? layoutDto.getCols() : 12);
            defaultLayout.put("rowHeight", layoutDto.getRowHeight() != null ? layoutDto.getRowHeight() : 42);
            defaultLayout.put("widgets", layoutDto.getWidgets() != null ? layoutDto.getWidgets() : new java.util.ArrayList<>());
            defaultLayout.put("options", layoutDto.getOptions() != null ? layoutDto.getOptions() : new java.util.HashMap<>());

            java.util.List<java.util.Map<String, Object>> layoutList = new java.util.ArrayList<>();
            layoutList.add(defaultLayout);

            configMap.put("layouts", layoutList);
            configMap.put("activeLayoutId", "layout_default");
            configMap.put("cols", layoutDto.getCols() != null ? layoutDto.getCols() : 12);
            configMap.put("rowHeight", layoutDto.getRowHeight() != null ? layoutDto.getRowHeight() : 42);
            configMap.put("widgets", layoutDto.getWidgets() != null ? layoutDto.getWidgets() : new java.util.ArrayList<>());
            configMap.put("options", layoutDto.getOptions() != null ? layoutDto.getOptions() : new java.util.HashMap<>());
        }

        domain.setDetailLayoutConfig(configMap);
        domainRepository.save(domain);
        return configMap;
    }
}

