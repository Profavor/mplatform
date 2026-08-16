package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainPackageDto;
import com.classification.domain_system.dto.DomainPackageImportResult;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainPackageService {

    private final DomainRepository domainRepository;
    private final ClassificationAxisRepository axisRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final DqRuleRepository dqRuleRepository;
    private final MatchingRuleRepository matchingRuleRepository;
    private final WorkflowConfigRepository workflowConfigRepository;

    @Transactional(readOnly = true)
    public DomainPackageDto exportDomainPackage(UUID domainId, String requestedBy) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + domainId));

        DomainPackageDto.DomainInfo domainInfo = DomainPackageDto.DomainInfo.builder()
                .name(domain.getName())
                .description(domain.getDescription())
                .build();

        // 1. Axes
        List<ClassificationAxis> axes = axisRepository.findByDomainIdOrderBySortOrderAsc(domainId);
        List<DomainPackageDto.AxisInfo> axisInfos = axes.stream()
                .map(a -> DomainPackageDto.AxisInfo.builder()
                        .name(a.getName())
                        .axisCode(a.getAxisCode())
                        .isDefault(a.getIsDefault())
                        .sortOrder(a.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        // 2. Nodes
        List<ClassificationNode> nodes = nodeRepository.findByDomain_Id(domainId);
        Map<UUID, String> nodeKeyMap = new HashMap<>();
        int keyIndex = 1;
        for (ClassificationNode n : nodes) {
            nodeKeyMap.put(n.getId(), "NODE_" + (keyIndex++));
        }

        List<DomainPackageDto.NodeInfo> nodeInfos = nodes.stream()
                .map(n -> DomainPackageDto.NodeInfo.builder()
                        .nodeKey(nodeKeyMap.get(n.getId()))
                        .parentNodeKey(n.getParent() != null ? nodeKeyMap.get(n.getParent().getId()) : null)
                        .name(n.getName())
                        .sortOrder(n.getOrder())
                        .axisCode(n.getAxis() != null ? n.getAxis().getAxisCode() : null)
                        .build())
                .collect(Collectors.toList());

        // 3. Fields
        List<FieldDefinition> domainFields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<UUID> nodeIds = nodes.stream().map(ClassificationNode::getId).collect(Collectors.toList());
        List<FieldDefinition> nodeFields = !nodeIds.isEmpty() ? fieldDefinitionRepository.findByDefinedAtNode_IdIn(nodeIds) : Collections.emptyList();
        List<FieldDefinition> fields = new ArrayList<>(domainFields);
        fields.addAll(nodeFields);

        List<DomainPackageDto.FieldInfo> fieldInfos = fields.stream()
                .map(f -> DomainPackageDto.FieldInfo.builder()
                        .nodeKey(f.getDefinedAtNode() != null ? nodeKeyMap.get(f.getDefinedAtNode().getId()) : null)
                        .key(f.getKey())
                        .name(f.getName())
                        .type(f.getType())
                        .required(f.getRequired())
                        .isEncrypted(f.getIsEncrypted())
                        .maskingPattern(f.getMaskingPattern())
                        .order(f.getOrder())
                        .options(f.getOptions())
                        .build())
                .collect(Collectors.toList());

        // 4. DQ Rules
        List<DqRule> dqRules = dqRuleRepository.findByDomainIdAndIsActiveTrueOrderBySortOrderAsc(domainId);
        List<DomainPackageDto.DqRuleInfo> dqRuleInfos = dqRules.stream()
                .map(r -> DomainPackageDto.DqRuleInfo.builder()
                        .fieldKey(r.getFieldDefinition() != null ? r.getFieldDefinition().getKey() : null)
                        .ruleType(r.getRuleType() != null ? r.getRuleType().name() : null)
                        .params(r.getParams())
                        .severity(r.getSeverity() != null ? r.getSeverity().name() : "ERROR")
                        .message(r.getMessage())
                        .build())
                .collect(Collectors.toList());

        // 5. Matching Rules
        List<MatchingRule> matchingRules = matchingRuleRepository.findByDomainId(domainId);
        List<DomainPackageDto.MatchingRuleInfo> matchingRuleInfos = matchingRules.stream()
                .map(m -> DomainPackageDto.MatchingRuleInfo.builder()
                        .ruleName(m.getRuleName())
                        .matchType(m.getMatchType())
                        .targetFieldKeys(m.getTargetFieldKeys())
                        .similarityThreshold(m.getSimilarityThreshold())
                        .build())
                .collect(Collectors.toList());

        // 6. Workflows
        List<WorkflowConfig> workflows = workflowConfigRepository.findByDomainId(domainId);
        List<DomainPackageDto.WorkflowInfo> workflowInfos = workflows.stream()
                .map(w -> DomainPackageDto.WorkflowInfo.builder()
                        .nodeKey(w.getNode() != null ? nodeKeyMap.get(w.getNode().getId()) : null)
                        .actionType(w.getActionType() != null ? w.getActionType() : "CREATE")
                        .name(w.getName())
                        .description(w.getDescription())
                        .stepsConfig(w.getStepsConfig())
                        .isDefault(w.getIsDefault())
                        .isActive(w.getIsActive())
                        .build())
                .collect(Collectors.toList());

        return DomainPackageDto.builder()
                .version("1.0")
                .exportedAt(LocalDateTime.now())
                .exportedBy(requestedBy)
                .domain(domainInfo)
                .axes(axisInfos)
                .nodes(nodeInfos)
                .fields(fieldInfos)
                .dqRules(dqRuleInfos)
                .matchingRules(matchingRuleInfos)
                .workflows(workflowInfos)
                .build();
    }

    @Transactional
    public DomainPackageImportResult importDomainPackage(DomainPackageDto pkg, String requestedBy, boolean overwrite) {
        if (pkg == null || pkg.getDomain() == null || pkg.getDomain().getName() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Invalid domain package content.");
        }

        DomainPackageDto.DomainInfo dInfo = pkg.getDomain();
        String primaryName = dInfo.getName().getOrDefault("ko", dInfo.getName().getOrDefault("en", "Unnamed Domain"));
        
        List<Domain> allDomains = domainRepository.findAll();
        Optional<Domain> existing = allDomains.stream()
                .filter(d -> d.getName() != null && d.getName().values().stream().anyMatch(n -> n.equalsIgnoreCase(primaryName)))
                .findFirst();

        Domain domain;
        if (existing.isPresent()) {
            if (!overwrite) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "Domain already exists: " + primaryName);
            }
            domain = existing.get();
            domain.setName(dInfo.getName());
            if (dInfo.getDescription() != null) domain.setDescription(dInfo.getDescription());
            domain = domainRepository.save(domain);
        } else {
            domain = new Domain();
            domain.setName(dInfo.getName());
            domain.setDescription(dInfo.getDescription() != null ? dInfo.getDescription() : Map.of());
            domain = domainRepository.save(domain);
        }

        UUID domainId = domain.getId();

        // 1. Axes
        Map<String, ClassificationAxis> axisMap = new HashMap<>();
        int axisCount = 0;
        if (pkg.getAxes() != null) {
            for (DomainPackageDto.AxisInfo a : pkg.getAxes()) {
                if (a.getAxisCode() == null) continue;
                ClassificationAxis axis = axisRepository.findByDomainIdAndAxisCode(domainId, a.getAxisCode())
                        .orElseGet(ClassificationAxis::new);
                axis.setDomain(domain);
                axis.setName(a.getName() != null ? a.getName() : Map.of("ko", a.getAxisCode()));
                axis.setAxisCode(a.getAxisCode());
                axis.setIsDefault(Boolean.TRUE.equals(a.getIsDefault()));
                axis.setSortOrder(a.getSortOrder() != null ? a.getSortOrder() : 0);
                ClassificationAxis savedAxis = axisRepository.save(axis);
                axisMap.put(a.getAxisCode(), savedAxis);
                axisCount++;
            }
        }

        // 2. Nodes
        List<ClassificationNode> existingNodes = nodeRepository.findByDomain_Id(domainId);
        Map<String, ClassificationNode> nodeMap = new HashMap<>();
        int nodeCount = 0;
        if (pkg.getNodes() != null) {
            for (DomainPackageDto.NodeInfo n : pkg.getNodes()) {
                Optional<ClassificationNode> matchedNode = existingNodes.stream()
                        .filter(en -> isSameNodeName(en.getName(), n.getName()))
                        .findFirst();

                ClassificationNode node;
                if (matchedNode.isPresent()) {
                    node = matchedNode.get();
                    if (n.getName() != null) node.setName(n.getName());
                    if (n.getSortOrder() != null) node.setOrder(n.getSortOrder());
                } else {
                    node = new ClassificationNode();
                    node.setDomain(domain);
                    node.setName(n.getName() != null ? n.getName() : Map.of("ko", "Node"));
                    node.setOrder(n.getSortOrder() != null ? n.getSortOrder() : 0);
                    node.setDepth(0);
                    node.setPath("/");
                    node.setIsDeleted(false);
                }

                if (n.getAxisCode() != null && axisMap.containsKey(n.getAxisCode())) {
                    node.setAxis(axisMap.get(n.getAxisCode()));
                }
                node = nodeRepository.save(node);
                if (node.getPath() == null || node.getPath().equals("/") || node.getPath().isBlank()) {
                    node.setPath("/" + node.getId());
                    node = nodeRepository.save(node);
                }

                nodeMap.put(n.getNodeKey(), node);
                nodeCount++;
            }

            // Set parent references & update depth/path
            for (DomainPackageDto.NodeInfo n : pkg.getNodes()) {
                if (n.getParentNodeKey() != null && nodeMap.containsKey(n.getParentNodeKey())) {
                    ClassificationNode child = nodeMap.get(n.getNodeKey());
                    ClassificationNode parent = nodeMap.get(n.getParentNodeKey());
                    if (child != null && parent != null && !Objects.equals(child.getId(), parent.getId())) {
                        child.setParent(parent);
                        child.setDepth(parent.getDepth() != null ? parent.getDepth() + 1 : 1);
                        child.setPath((parent.getPath() != null ? parent.getPath() : "") + "/" + child.getId());
                        nodeRepository.save(child);
                    }
                }
            }
        }

        // 3. Fields
        List<FieldDefinition> existingDomainFields = fieldDefinitionRepository.findByDomain_Id(domainId);
        Map<String, FieldDefinition> fieldMap = new HashMap<>();
        int fieldCount = 0;
        if (pkg.getFields() != null) {
            for (DomainPackageDto.FieldInfo f : pkg.getFields()) {
                if (f.getKey() == null || f.getKey().isBlank()) continue;
                ClassificationNode targetNode = (f.getNodeKey() != null && nodeMap.containsKey(f.getNodeKey()))
                        ? nodeMap.get(f.getNodeKey()) : null;
                UUID targetNodeId = targetNode != null ? targetNode.getId() : null;

                Optional<FieldDefinition> matchedField = existingDomainFields.stream()
                        .filter(ef -> f.getKey().equalsIgnoreCase(ef.getKey()))
                        .filter(ef -> {
                            UUID efNodeId = ef.getDefinedAtNode() != null ? ef.getDefinedAtNode().getId() : null;
                            return Objects.equals(efNodeId, targetNodeId);
                        })
                        .findFirst();

                FieldDefinition fd;
                if (matchedField.isPresent()) {
                    fd = matchedField.get();
                } else {
                    fd = new FieldDefinition();
                    fd.setDomain(domain);
                    fd.setKey(f.getKey());
                }

                if (targetNode != null) {
                    fd.setDefinedAtNode(targetNode);
                }
                fd.setName(f.getName() != null ? f.getName() : Map.of("ko", f.getKey()));
                fd.setType(f.getType() != null ? f.getType() : "STRING");
                fd.setRequired(Boolean.TRUE.equals(f.getRequired()));
                fd.setIsEncrypted(Boolean.TRUE.equals(f.getIsEncrypted()));
                fd.setMaskingPattern(f.getMaskingPattern());
                fd.setOrder(f.getOrder() != null ? f.getOrder() : 0);
                fd.setOptions(f.getOptions());

                fd = fieldDefinitionRepository.save(fd);
                fieldMap.put(f.getKey(), fd);
                fieldCount++;
            }
        }

        // 4. DQ Rules
        List<DqRule> existingDqRules = dqRuleRepository.findByDomainId(domainId);
        int dqRuleCount = 0;
        if (pkg.getDqRules() != null) {
            for (DomainPackageDto.DqRuleInfo r : pkg.getDqRules()) {
                if (r.getFieldKey() != null && fieldMap.containsKey(r.getFieldKey())) {
                    FieldDefinition fd = fieldMap.get(r.getFieldKey());
                    DqRuleType rType = null;
                    try {
                        rType = DqRuleType.valueOf(r.getRuleType());
                    } catch (Exception ignored) {}

                    final DqRuleType finalRType = rType;
                    Optional<DqRule> matchedDq = existingDqRules.stream()
                            .filter(er -> er.getFieldDefinition() != null && Objects.equals(er.getFieldDefinition().getId(), fd.getId()))
                            .filter(er -> er.getRuleType() == finalRType)
                            .findFirst();

                    DqRule rule = matchedDq.orElseGet(DqRule::new);
                    rule.setFieldDefinition(fd);
                    rule.setDomainId(domainId);
                    rule.setRuleType(rType);
                    rule.setParams(r.getParams());
                    try {
                        rule.setSeverity(DqSeverity.valueOf(r.getSeverity()));
                    } catch (Exception e) {
                        rule.setSeverity(DqSeverity.ERROR);
                    }
                    rule.setMessage(r.getMessage() != null ? r.getMessage() : Map.of());
                    rule.setIsActive(true);
                    dqRuleRepository.save(rule);
                    dqRuleCount++;
                }
            }
        }

        // 5. Matching Rules
        List<MatchingRule> existingMatchingRules = matchingRuleRepository.findByDomainId(domainId);
        int matchingRuleCount = 0;
        if (pkg.getMatchingRules() != null) {
            for (DomainPackageDto.MatchingRuleInfo m : pkg.getMatchingRules()) {
                Optional<MatchingRule> matched = existingMatchingRules.stream()
                        .filter(em -> Objects.equals(em.getRuleName(), m.getRuleName()))
                        .findFirst();

                MatchingRule rule = matched.orElseGet(MatchingRule::new);
                rule.setDomainId(domainId);
                rule.setRuleName(m.getRuleName());
                rule.setMatchType(m.getMatchType());
                rule.setTargetFieldKeys(m.getTargetFieldKeys());
                rule.setSimilarityThreshold(m.getSimilarityThreshold() != null ? m.getSimilarityThreshold() : 0.85);
                rule.setActive(true);
                matchingRuleRepository.save(rule);
                matchingRuleCount++;
            }
        }

        // 6. Workflows
        List<WorkflowConfig> existingWorkflows = workflowConfigRepository.findByDomainId(domainId);
        int workflowCount = 0;
        if (pkg.getWorkflows() != null) {
            for (DomainPackageDto.WorkflowInfo w : pkg.getWorkflows()) {
                String actionType = w.getActionType() != null && !w.getActionType().isBlank() ? w.getActionType() : "CREATE";
                ClassificationNode node = (w.getNodeKey() != null && nodeMap.containsKey(w.getNodeKey()))
                        ? nodeMap.get(w.getNodeKey()) : null;
                UUID targetNodeId = node != null ? node.getId() : null;

                Optional<WorkflowConfig> matched = existingWorkflows.stream()
                        .filter(ew -> Objects.equals(ew.getActionType(), actionType) && Objects.equals(ew.getNodeId(), targetNodeId))
                        .findFirst();

                WorkflowConfig wf = matched.orElseGet(WorkflowConfig::new);
                wf.setDomain(domain);
                if (node != null) {
                    wf.setNode(node);
                    wf.setNodeId(node.getId());
                }
                wf.setActionType(actionType);
                wf.setName(w.getName());
                wf.setDescription(w.getDescription());
                wf.setStepsConfig(w.getStepsConfig());
                wf.setIsDefault(Boolean.TRUE.equals(w.getIsDefault()));
                wf.setIsActive(w.getIsActive() != null ? w.getIsActive() : true);
                workflowConfigRepository.save(wf);
                workflowCount++;
            }
        }

        return DomainPackageImportResult.builder()
                .domainId(domainId)
                .domainName(primaryName)
                .axisCount(axisCount)
                .nodeCount(nodeCount)
                .fieldCount(fieldCount)
                .dqRuleCount(dqRuleCount)
                .matchingRuleCount(matchingRuleCount)
                .workflowCount(workflowCount)
                .success(true)
                .message("Domain package imported successfully.")
                .build();
    }

    private boolean isSameNodeName(Map<String, String> name1, Map<String, String> name2) {
        if (name1 == null || name2 == null) return false;
        for (Map.Entry<String, String> e : name1.entrySet()) {
            String val1 = e.getValue();
            String val2 = name2.get(e.getKey());
            if (val1 != null && val2 != null && val1.trim().equalsIgnoreCase(val2.trim())) {
                return true;
            }
        }
        for (String v1 : name1.values()) {
            for (String v2 : name2.values()) {
                if (v1 != null && v2 != null && v1.trim().equalsIgnoreCase(v2.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
