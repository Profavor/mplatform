package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DomainPermission;
import com.classification.domain_system.entity.DomainAccessRequest;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.DomainPermissionRepository;
import com.classification.domain_system.repository.DomainAccessRequestRepository;
import com.classification.domain_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DomainPermissionService {

    private final DomainPermissionRepository permissionRepository;
    private final DomainAccessRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;

    @Transactional
    public void grantPermission(String userId, UUID domainId) {
        Optional<DomainPermission> existing = permissionRepository.findByUserIdAndDomainId(userId, domainId);
        if (existing.isEmpty()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Domain domain = domainRepository.findById(domainId).orElseThrow(() -> new RuntimeException("Domain not found"));
            
            DomainPermission permission = new DomainPermission();
            permission.setUser(user);
            permission.setDomain(domain);
            permissionRepository.save(permission);
        }
    }

    @Transactional
    public void revokePermission(String userId, UUID domainId) {
        permissionRepository.deleteByUserIdAndDomainId(userId, domainId);
    }

    @Transactional(readOnly = true)
    public List<DomainPermission> getUserPermissions(String userId) {
        return permissionRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Domain> getAvailableDomains(String userId) {
        List<Domain> allDomains = domainRepository.findAllByOrderBySortOrderAsc();
        List<UUID> grantedIds = permissionRepository.findByUserId(userId).stream()
                .map(dp -> dp.getDomain().getId()).toList();
        return allDomains.stream()
                .filter(d -> !grantedIds.contains(d.getId()))
                .toList();
    }

    // Access Requests
    @Transactional
    public DomainAccessRequest requestAccess(String userId, UUID domainId) {
        Optional<DomainAccessRequest> existing = requestRepository.findByUserIdAndDomainIdAndStatus(userId, domainId, "PENDING");
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Domain domain = domainRepository.findById(domainId).orElseThrow(() -> new RuntimeException("Domain not found"));

        DomainAccessRequest request = new DomainAccessRequest();
        request.setUser(user);
        request.setDomain(domain);
        request.setStatus("PENDING");
        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<DomainAccessRequest> getPendingRequests() {
        return requestRepository.findByStatus("PENDING");
    }

    @Transactional
    public void approveRequest(UUID requestId) {
        DomainAccessRequest req = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus("APPROVED");
        requestRepository.save(req);
        grantPermission(req.getUser().getId(), req.getDomain().getId());
    }

    @Transactional
    public void rejectRequest(UUID requestId) {
        DomainAccessRequest req = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus("REJECTED");
        requestRepository.save(req);
    }

    @Transactional
    public void cancelRequest(UUID requestId, String userId) {
        DomainAccessRequest req = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        if (userId != null && !userId.equals(req.getUser().getId()) && !userId.equals(req.getUser().getUsername())) {
            throw new RuntimeException("Unauthorized to cancel this request");
        }
        requestRepository.delete(req);
    }
}
