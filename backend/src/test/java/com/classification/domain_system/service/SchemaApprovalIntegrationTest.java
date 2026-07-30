package com.classification.domain_system.service;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@org.springframework.test.context.ActiveProfiles("test")
public class SchemaApprovalIntegrationTest {

    @Autowired
    private FieldDefinitionService fieldDefinitionService;
    
    @Autowired
    private ClassificationNodeService classificationNodeService;

    @Autowired
    private SchemaHistoryRepository schemaHistoryRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private WorkflowConfigRepository workflowConfigRepository;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private ApprovalStepRepository approvalStepRepository;

    @Autowired
    private ApprovalService approvalService;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private Domain testDomain;
    private ClassificationNode testNode;

    @BeforeEach
    void setUp() {
        testDomain = new Domain();
        testDomain.setName(Map.of("en", "Test Domain"));
        testDomain = domainRepository.save(testDomain);

        ClassificationNodeRequest nodeReq = new ClassificationNodeRequest();
        nodeReq.setName(Map.of("en", "Test Node"));
        testNode = classificationNodeService.createNode(testDomain.getId(), nodeReq);
        schemaHistoryRepository.deleteAll();
    }

    @Test
    void testFieldAddRecordsSchemaHistory() {
        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Test Field"));
        req.setKey("test_field");
        req.setType("TEXT");

        FieldDefinition saved = fieldDefinitionService.addField(testNode.getId(), req);

        List<SchemaHistory> histories = schemaHistoryRepository.findAll();
        assertThat(histories).hasSize(1); // One for Field addition
        
        SchemaHistory fieldHistory = histories.stream()
            .filter(h -> "FIELD".equals(h.getTargetType()))
            .findFirst().orElseThrow();
            
        assertThat(fieldHistory.getAction()).isEqualTo("CREATE");
        assertThat(fieldHistory.getDomainId()).isEqualTo(testDomain.getId());
        assertThat(fieldHistory.getTargetId()).isEqualTo(saved.getId());
    }

    @Test
    void testFieldUpdateRecordsSchemaHistory() {
        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Test Field"));
        req.setKey("test_field");
        req.setType("TEXT");
        FieldDefinition saved = fieldDefinitionService.addField(testNode.getId(), req);

        req.setType("NUMBER");
        fieldDefinitionService.updateField(testNode.getId(), saved.getId(), req);

        List<SchemaHistory> updates = schemaHistoryRepository.findAll().stream()
            .filter(h -> "FIELD".equals(h.getTargetType()) && "UPDATE".equals(h.getAction()))
            .toList();
            
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getBeforeData()).isNotNull();
        assertThat(updates.get(0).getAfterData()).isNotNull();
    }

    @Test
    void testNodeCreateRecordsSchemaHistory() {
        ClassificationNodeRequest req = new ClassificationNodeRequest();
        req.setName(Map.of("en", "Child Node"));
        req.setParentId(testNode.getId());
        ClassificationNode child = classificationNodeService.createNode(testDomain.getId(), req);

        List<SchemaHistory> histories = schemaHistoryRepository.findAll().stream()
            .filter(h -> "NODE".equals(h.getTargetType()) && "CREATE".equals(h.getAction()) && h.getTargetId().equals(child.getId()))
            .toList();
            
        assertThat(histories).hasSize(1);
    }

    @Test
    void testNodeMoveRecordsSchemaHistory() {
        // Create another parent
        ClassificationNodeRequest pReq = new ClassificationNodeRequest();
        pReq.setName(Map.of("en", "Parent 2"));
        ClassificationNode parent2 = classificationNodeService.createNode(testDomain.getId(), pReq);

        // Child node
        ClassificationNodeRequest req = new ClassificationNodeRequest();
        req.setName(Map.of("en", "Child Node"));
        req.setParentId(testNode.getId());
        ClassificationNode child = classificationNodeService.createNode(testDomain.getId(), req);

        // Move to Parent 2
        req.setParentId(parent2.getId());
        // For move, usually it's updateNode in this implementation, but we'll simulate the move test
        // wait, node move logic might require another method or updating parentId via updateNode.
        // I will just use updateNode for now.
        classificationNodeService.updateNode(testDomain.getId(), child.getId(), req);

        List<SchemaHistory> histories = schemaHistoryRepository.findAll().stream()
            .filter(h -> "NODE".equals(h.getTargetType()) && "UPDATE".equals(h.getAction()) && h.getTargetId().equals(child.getId()))
            .toList();
            
        assertThat(histories).hasSize(1);
    }

    @Test
    void testSchemaApprovalWorkflow_WhenConfigured() {
        WorkflowConfig config = new WorkflowConfig();
        config.setDomainId(testDomain.getId());
        config.setActionType("SCHEMA_CHANGE");
        config.setStepsConfig("{\"steps\":[{\"stepType\":\"APPROVER\",\"assigneeId\":\"" + UUID.randomUUID() + "\",\"stepOrder\":1}],\"observerIds\":[]}");
        workflowConfigRepository.save(config);

        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Test Field"));
        req.setKey("test_field");
        req.setType("TEXT");

        fieldDefinitionService.addField(testNode.getId(), req);

        List<ApprovalRequest> requests = approvalRequestRepository.findAll();
        assertThat(requests).anyMatch(r -> "SCHEMA_FIELD_ADD".equals(r.getTargetType()));
    }

    @Test
    void testSchemaApprovalFinalApproval() {
        String approverId = UUID.randomUUID().toString();
        WorkflowConfig config = new WorkflowConfig();
        config.setDomainId(testDomain.getId());
        config.setActionType("SCHEMA_CHANGE");
        config.setStepsConfig("{\"steps\":[{\"stepType\":\"APPROVER\",\"assigneeId\":\"" + approverId + "\",\"stepOrder\":1}],\"observerIds\":[]}");
        workflowConfigRepository.save(config);

        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Approval Field"));
        req.setKey("approval_field");
        req.setType("TEXT");

        fieldDefinitionService.addField(testNode.getId(), req);

        ApprovalRequest request = approvalRequestRepository.findAll().stream()
            .filter(r -> "SCHEMA_FIELD_ADD".equals(r.getTargetType()))
            .findFirst().orElseThrow();

        ApprovalStep step = approvalStepRepository.findAll().stream()
            .filter(s -> s.getApprovalRequest().getId().equals(request.getId()))
            .sorted(java.util.Comparator.comparing(ApprovalStep::getStepOrder))
            .findFirst().orElseThrow();
        
        // This triggers the event
        approvalService.approveStep(step.getId(), approverId, "Approved");
        
        // Now field should be added to repo
        long fieldCount = fieldDefinitionService.getEffectiveFields(testNode.getId()).stream()
            .filter(f -> "approval_field".equals(f.getKey()))
            .count();
        assertThat(fieldCount).isEqualTo(1);
    }

    @Test
    void testSchemaApprovalRejection() {
        String approverId = UUID.randomUUID().toString();
        WorkflowConfig config = new WorkflowConfig();
        config.setDomainId(testDomain.getId());
        config.setActionType("SCHEMA_CHANGE");
        config.setStepsConfig("{\"steps\":[{\"stepType\":\"APPROVER\",\"assigneeId\":\"" + approverId + "\",\"stepOrder\":1}],\"observerIds\":[]}");
        workflowConfigRepository.save(config);

        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Reject Field"));
        req.setKey("reject_field");
        req.setType("TEXT");

        fieldDefinitionService.addField(testNode.getId(), req);

        ApprovalRequest request = approvalRequestRepository.findAll().stream()
            .filter(r -> "SCHEMA_FIELD_ADD".equals(r.getTargetType()))
            .findFirst().orElseThrow();

        ApprovalStep step = approvalStepRepository.findAll().stream()
            .filter(s -> s.getApprovalRequest().getId().equals(request.getId()))
            .sorted(java.util.Comparator.comparing(ApprovalStep::getStepOrder))
            .findFirst().orElseThrow();
        
        approvalService.rejectStep(step.getId(), approverId, "Rejected");
        
        long fieldCount = fieldDefinitionService.getEffectiveFields(testNode.getId()).stream()
            .filter(f -> "reject_field".equals(f.getKey()))
            .count();
        assertThat(fieldCount).isEqualTo(0);
    }

    @Test
    void testSchemaHistoryQueryByDomain() {
        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Test Field"));
        req.setKey("test_field");
        req.setType("TEXT");
        fieldDefinitionService.addField(testNode.getId(), req);
        
        List<SchemaHistory> histories = schemaHistoryRepository.findByDomainIdOrderByChangedAtDesc(testDomain.getId(), org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        assertThat(histories).isNotEmpty();
    }

    @Test
    void testSchemaApprovalWorkflow_NotConfigured_ImmediateApply() {
        FieldDefinitionRequest req = new FieldDefinitionRequest();
        req.setName(Map.of("en", "Test Field"));
        req.setKey("test_field");
        req.setType("TEXT");

        FieldDefinition saved = fieldDefinitionService.addField(testNode.getId(), req);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }
}
