package com.classification.domain_system.service;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.entity.enums.ApprovalTargetType;
import com.classification.domain_system.entity.enums.RecordStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalQueryService {

    private final ApprovalRequestRepository approvalRepository;
    private final ApprovalStepRepository stepRepository;
    private final DomainRepository domainRepository;
    @SuppressWarnings("deprecation")
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final FieldDefinitionService fieldDefinitionService;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final DataMaskingService dataMaskingService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @org.springframework.context.annotation.Lazy
    private final ApprovalDelegationService delegationService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @org.springframework.context.annotation.Lazy
    private RecordService recordService;

    private static final Set<String> ALLOWED_FILTER_KEYS = Set.of(
            "domainName", "classificationName", "requesterId", "changes", "summary",
            "status", "targetType", "targetId", "id", "currentStepOrder", "createdAt", "updatedAt"
    );

    public String resolveRoleDisplayName(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return "";
        if (roleRepository == null) return roleCode;
        try {
            List<Role> roles = roleRepository.findAll();
            for (Role r : roles) {
                if (r.getName() != null) {
                    if (r.getName().equalsIgnoreCase(roleCode) || r.getName().equalsIgnoreCase("ROLE_" + roleCode) || ("ROLE_" + r.getName()).equalsIgnoreCase(roleCode)) {
                        if (r.getDisplayName() != null && !r.getDisplayName().isBlank()) {
                            String raw = r.getDisplayName().trim();
                            if (raw.startsWith("{") && raw.endsWith("}")) {
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    JsonNode node = mapper.readTree(raw);
                                    if (node.has("ko") && !node.get("ko").asText().isBlank()) {
                                        return node.get("ko").asText();
                                    } else if (node.has("en") && !node.get("en").asText().isBlank()) {
                                        return node.get("en").asText();
                                    }
                                } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {}
                            }
                            return raw;
                        }
                    }
                }
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.warn("Failed to load roles for display name resolution: {}", e.getMessage());
        }
        return roleCode;
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getPendingRequests(Pageable pageable) {
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING.name(), pageable);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Page<ApprovalRequest> getAllRequests(String search, String status, String filterModel, Pageable pageable) {
        final Sort sort = pageable.getSort();
        
        Specification<ApprovalRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (status != null && !status.trim().isEmpty()) {
                String[] statuses = status.split(",");
                CriteriaBuilder.In<String> inClause = cb.in(root.get("status"));
                for (String s : statuses) {
                    inClause.value(s.trim());
                }
                predicates.add(inClause);
            }
            
            if (filterModel != null && !filterModel.trim().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(filterModel);
                    Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
                    
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> fieldEntry = fields.next();
                        String fieldKey = fieldEntry.getKey();
                        JsonNode filterInfo = fieldEntry.getValue();
                        
                        if (!ALLOWED_FILTER_KEYS.contains(fieldKey)) {
                            throw new BusinessException(ErrorCode.INVALID_INPUT, "Filter key not allowed: " + fieldKey);
                        }
                        
                        if (filterInfo.has("filterType")) {
                            String filterType = filterInfo.get("filterType").asText();
                            
                            if ("set".equals(filterType) && filterInfo.has("values")) {
                                List<String> vals = new ArrayList<>();
                                for (JsonNode vNode : filterInfo.get("values")) {
                                    vals.add(vNode.asText());
                                }
                                if (!vals.isEmpty()) {
                                    if ("domainName".equals(fieldKey) || "classificationName".equals(fieldKey)) {
                                        Join<Object, Object> nodeJoin = root.join("classificationNode", JoinType.LEFT);
                                        if ("classificationName".equals(fieldKey)) {
                                            List<Predicate> orPreds = new ArrayList<>();
                                            for (String v : vals) {
                                                orPreds.add(cb.like(cb.lower(nodeJoin.get("name").as(String.class)), "%" + v.toLowerCase() + "%"));
                                            }
                                            predicates.add(cb.or(orPreds.toArray(new Predicate[0])));
                                        } else {
                                            Join<Object, Object> domainJoin = nodeJoin.join("domain", JoinType.LEFT);
                                            List<Predicate> orPreds = new ArrayList<>();
                                            for (String v : vals) {
                                                orPreds.add(cb.like(cb.lower(domainJoin.get("name").as(String.class)), "%" + v.toLowerCase() + "%"));
                                            }
                                            predicates.add(cb.or(orPreds.toArray(new Predicate[0])));
                                        }
                                    } else {
                                        CriteriaBuilder.In<String> inClause = cb.in(root.get(fieldKey));
                                        for (String v : vals) {
                                            inClause.value(v);
                                        }
                                        predicates.add(inClause);
                                    }
                                }
                            }
                            else if ("text".equals(filterType) && filterInfo.has("filter")) {
                                String textValue = filterInfo.get("filter").asText().trim().toLowerCase();
                                String type = filterInfo.has("type") ? filterInfo.get("type").asText() : "contains";
                                String likePattern = "%" + textValue + "%";
                                if ("equals".equals(type)) likePattern = textValue;
                                else if ("startsWith".equals(type)) likePattern = textValue + "%";
                                else if ("endsWith".equals(type)) likePattern = "%" + textValue;
                                
                                if ("domainName".equals(fieldKey) || "classificationName".equals(fieldKey)) {
                                    Join<Object, Object> nodeJoin = root.join("classificationNode", JoinType.LEFT);
                                    if ("classificationName".equals(fieldKey)) {
                                        predicates.add(cb.like(cb.lower(nodeJoin.get("name").as(String.class)), likePattern));
                                    } else {
                                        Join<Object, Object> domainJoin = nodeJoin.join("domain", JoinType.LEFT);
                                        predicates.add(cb.like(cb.lower(domainJoin.get("name").as(String.class)), likePattern));
                                    }
                                } else if ("requesterId".equals(fieldKey)) {
                                    predicates.add(cb.like(cb.lower(root.get("requesterId").as(String.class)), likePattern));
                                } else if ("changes".equals(fieldKey) || "summary".equals(fieldKey)) {
                                    Expression<String> changesText = cb.function("concat", String.class, root.get("changes"), cb.literal(""));
                                    predicates.add(cb.like(cb.lower(changesText), likePattern));
                                } else {
                                    predicates.add(cb.like(cb.lower(root.get(fieldKey).as(String.class)), likePattern));
                                }
                            }
                            else if ("date".equals(filterType) && filterInfo.has("dateFrom")) {
                                String type = filterInfo.has("type") ? filterInfo.get("type").asText() : "equals";
                                String dateFromStr = filterInfo.get("dateFrom").asText();
                                String dateToStr = filterInfo.has("dateTo") && !filterInfo.get("dateTo").isNull() ? filterInfo.get("dateTo").asText() : null;
                                
                                LocalDateTime dateFrom = com.classification.domain_system.utils.DateTimeUtils.parseDateTime(dateFromStr);
                                LocalDateTime dateTo = dateToStr != null ? com.classification.domain_system.utils.DateTimeUtils.parseDateTime(dateToStr) : null;
                                
                                if (dateFrom != null) {
                                    if ("equals".equals(type)) {
                                        predicates.add(cb.between(root.get(fieldKey), dateFrom, dateFrom.plusDays(1).minusSeconds(1)));
                                    } else if ("greaterThan".equals(type)) {
                                        predicates.add(cb.greaterThan(root.get(fieldKey), dateFrom));
                                    } else if ("lessThan".equals(type)) {
                                        predicates.add(cb.lessThan(root.get(fieldKey), dateFrom));
                                    } else if ("inRange".equals(type) && dateTo != null) {
                                        predicates.add(cb.between(root.get(fieldKey), dateFrom, dateTo));
                                    }
                                }
                            }
                        }
                    }
                } catch (BusinessException be) {
                    throw be;
                } catch (com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException e) {
                    log.error("Failed to process dynamic search predicate", e);
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "Invalid search filter: " + e.getMessage());
                } catch (RuntimeException e) {
                    log.error("Failed to process dynamic search predicate", e);
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Search predicate processing failed");
                }
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                Join<Object, Object> nodeJoin = root.join("classificationNode", JoinType.LEFT);
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("targetType")), likePattern),
                    cb.like(cb.lower(root.get("status")), likePattern),
                    cb.like(cb.lower(cb.function("concat", String.class, root.get("changes"), cb.literal(""))), likePattern),
                    cb.like(cb.lower(nodeJoin.get("name").as(String.class)), likePattern)
                ));
            }
            
            boolean isCountQuery = Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());
            if (!isCountQuery && sort != null && sort.isSorted()) {
                Map<UUID, String> idKeyMap = new HashMap<>();
                Map<UUID, String> nameKeyMap = new HashMap<>();
                try {
                    List<Domain> domains = domainRepository.findAll();
                    for (Domain d : domains) {
                        if (d.getIdentifierFieldId() != null) {
                            FieldDefinition fd = fieldDefinitionRepository.findById(d.getIdentifierFieldId()).orElse(null);
                            if (fd != null) idKeyMap.put(d.getId(), fd.getKey());
                        }
                        if (d.getDisplayNameFieldId() != null) {
                            FieldDefinition fd = fieldDefinitionRepository.findById(d.getDisplayNameFieldId()).orElse(null);
                            if (fd != null) nameKeyMap.put(d.getId(), fd.getKey());
                        }
                    }
                } catch (org.springframework.dao.DataAccessException e) {
                    log.warn("Failed to load domain field key mappings for sorting: {}", e.getMessage());
                }

                List<Order> orders = new ArrayList<>();
                for (Sort.Order o : sort) {
                    String prop = o.getProperty();
                    boolean isAsc = o.isAscending();
                    
                    Expression<?> expression;
                    if ("classificationNode.name".equals(prop)) {
                        Join<Object, Object> nodeJoin = root.join("classificationNode", JoinType.LEFT);
                        expression = nodeJoin.get("name");
                    } else if ("classificationNode.domain.name".equals(prop)) {
                        Join<Object, Object> nodeJoin = root.join("classificationNode", JoinType.LEFT);
                        Join<Object, Object> domainJoin = nodeJoin.join("domain", JoinType.LEFT);
                        expression = domainJoin.get("name");
                    } else if ("idAttribute".equals(prop) || "nameAttribute".equals(prop)) {
                        try {
                            Join<Object, Object> nodeJoin = root.join("classificationNode", JoinType.LEFT);
                            Join<Object, Object> domainJoin = nodeJoin.join("domain", JoinType.LEFT);
                            Expression<UUID> domainIdExpr = domainJoin.get("id");
                            
                            Map<UUID, String> targetMap = "idAttribute".equals(prop) ? idKeyMap : nameKeyMap;
                            
                            CriteriaBuilder.Case<String> caseExpr = cb.selectCase();
                            
                            for (Map.Entry<UUID, String> entry : targetMap.entrySet()) {
                                String keyStr = entry.getValue();
                                
                                Expression<String> extractAfter = cb.function("jsonb_extract_path_text", String.class, 
                                    root.get("changes"), cb.literal("after"), cb.literal(keyStr));
                                Expression<String> extractData = cb.function("jsonb_extract_path_text", String.class, 
                                    root.get("changes"), cb.literal("data"), cb.literal(keyStr));
                                Expression<String> extractRoot = cb.function("jsonb_extract_path_text", String.class, 
                                    root.get("changes"), cb.literal(keyStr));
                                
                                Expression<String> extractText = cb.coalesce(
                                    extractAfter, 
                                    cb.coalesce(extractData, extractRoot)
                                );
                                
                                caseExpr = caseExpr.when(cb.equal(domainIdExpr, entry.getKey()), extractText);
                            }
                            
                            expression = caseExpr.otherwise(cb.literal(""));
                        } catch (Exception ex) {
                            log.error("Failed to build sort expression for displayName", ex);
                            expression = root.get("changes").as(String.class);
                        }
                    } else if ("summary".equals(prop)) {
                        expression = root.get("changes").as(String.class);
                    } else {
                        expression = root.get(prop);
                    }
                    
                    orders.add(isAsc ? cb.asc(expression) : cb.desc(expression));
                }
                query.orderBy(orders);
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return approvalRepository.findAll(spec, unsortedPageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, Pageable pageable) {
        return getMyTodos(assigneeId, (String) null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String userRole, Pageable pageable) {
        if (userRole != null && !userRole.isBlank()) {
            return stepRepository.findMyPendingSteps(assigneeId, userRole, ApprovalStatus.PENDING.name(), pageable);
        }
        return stepRepository.findByAssigneeIdAndStatus(assigneeId, ApprovalStatus.PENDING.name(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, Collection<String> userRoles, Pageable pageable) {
        if (userRoles != null && !userRoles.isEmpty()) {
            return stepRepository.findMyPendingStepsForRoles(assigneeId, userRoles, ApprovalStatus.PENDING.name(), pageable);
        }
        return getMyTodos(assigneeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String username, Collection<String> userRoles, Pageable pageable) {
        List<String> delegatorIds = delegationService != null ? delegationService.getActiveDelegatorIdsForDelegatee(assigneeId) : Collections.emptyList();
        if (delegatorIds != null && !delegatorIds.isEmpty()) {
            return stepRepository.findMyPendingStepsForUserAndRolesAndDelegators(
                    assigneeId, username, userRoles != null ? userRoles : Collections.emptyList(), delegatorIds, ApprovalStatus.PENDING.name(), pageable);
        }
        if (userRoles != null && !userRoles.isEmpty()) {
            return stepRepository.findMyPendingStepsForUserAndRoles(assigneeId, username, userRoles, ApprovalStatus.PENDING.name(), pageable);
        }
        return getMyTodos(assigneeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, Pageable pageable) {
        return approvalRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, String username, Pageable pageable) {
        return approvalRepository.findMyRequestsForUser(requesterId, username, pageable);
    }

    public ApprovalRequest maskChangesForRead(ApprovalRequest approval) {
        if (approval == null || approval.getChanges() == null || approval.getChanges().isBlank() || dataMaskingService == null) {
            return approval;
        }
        ClassificationNode node = approval.getClassificationNode();
        if (node == null && approval.getTargetId() != null && recordRepository != null) {
            node = recordRepository.findById(approval.getTargetId()).map(com.classification.domain_system.entity.Record::getNode).orElse(null);
        }
        if (node == null && approval.getChanges() != null && nodeRepository != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode parsed = mapper.readTree(approval.getChanges());
                if (parsed.has("nodeId") && !parsed.get("nodeId").isNull()) {
                    String nodeIdStr = parsed.get("nodeId").asText();
                    if (!nodeIdStr.isBlank()) {
                        node = nodeRepository.findById(UUID.fromString(nodeIdStr)).orElse(null);
                    }
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException ignored) {}
        }
        if (approval.getClassificationNode() == null && node != null) {
            approval.setClassificationNode(node);
        }
        List<FieldDefinition> fields;
        if (node != null && fieldDefinitionService != null) {
            fields = fieldDefinitionService.getEffectiveFields(node.getId());
        } else if (fieldDefinitionRepository != null) {
            fields = fieldDefinitionRepository.findAll();
        } else {
            fields = Collections.emptyList();
        }
        boolean canUnmask = false;
        String rawChanges = approval.getChanges();
        String maskedChanges = dataMaskingService.maskChangesJson(rawChanges, fields, canUnmask);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rawNode = mapper.readTree(rawChanges);
            if (rawNode.has("before") && rawNode.has("after") && recordService != null) {
                String prevJson = mapper.writeValueAsString(rawNode.get("before"));
                String newJson = mapper.writeValueAsString(rawNode.get("after"));
                List<String> changedFields = recordService.computeChangedFieldKeys(prevJson, newJson);

                JsonNode maskedNode = mapper.readTree(maskedChanges);
                if (maskedNode.isObject()) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) maskedNode).putPOJO("changedFields", changedFields);
                    maskedChanges = mapper.writeValueAsString(maskedNode);
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {}

        approval.setChanges(maskedChanges);
        if (approval.getSteps() == null || approval.getSteps().isEmpty()) {
            approval.setSteps(stepRepository.findByApprovalRequestIdOrderByStepOrderAsc(approval.getId()));
        }
        return approval;
    }

    @Transactional(readOnly = true)
    public ApprovalRequest getRequestById(UUID id) {
        ApprovalRequest req = approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest not found"));
        return maskChangesForRead(req);
    }
}
