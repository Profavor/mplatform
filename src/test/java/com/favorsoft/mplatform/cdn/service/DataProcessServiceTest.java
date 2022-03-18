package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.batch.DataProcessBatchService;
import com.favorsoft.mplatform.cdn.domain.*;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalUserType;
import com.favorsoft.mplatform.cdn.enums.FlowStep;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessFlowService;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessInstanceFlowService;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessInstanceService;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessService;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

@SpringBootTest
@ActiveProfiles("local")
public class DataProcessServiceTest {
    private static Logger logger = LoggerFactory.getLogger(DataProcessServiceTest.class);

    @Autowired
    private DataProcessService dataProcessService;

    @Autowired
    private DataProcessFlowService dataProcessFlowService;

    @Autowired
    private DataProcessInstanceService dataProcessInstanceService;

    @Autowired
    private DataProcessInstanceFlowService dataProcessInstanceFlowService;

    @Autowired
    private DataProcessBatchService dataProcessBatchService;

    @BeforeEach
    public void create() {
        logger.info("##### Spring security TEST_ACCOUNT 인증 #####");
        User user = new User("TEST_USER1");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        logger.info("##### APPROVAL [TEST_APPROVAL] CREATE #####");
        DataProcess approval = new DataProcess();
        approval.setProcessId("TEST_APPROVAL");
        approval.setMessage(new Message("PROP_TYPE_COMBO"));
        approval.setIsEnable("Y");
        approval.setValidDay(3);
        approval = dataProcessService.save(approval);

        logger.info("##### APPROVAL FLOW [TEST_APPROVAL] CREATE #####");
        DataProcessFlow approvalFlow1 = new DataProcessFlow();
        approvalFlow1.setProcessId("TEST_APPROVAL");
        approvalFlow1.setFlowStep(FlowStep.DRAFT);
        approvalFlow1.setFlowType("NORMAL");
        approvalFlow1.setApprovalUserType(ApprovalUserType.DRAFT);
        approvalFlow1.setUser(new User("TEST_USER1"));
        approvalFlow1.setNextFlowStep(FlowStep.STEP1);
        dataProcessFlowService.save(approvalFlow1);

        DataProcessFlow approvalFlow2 = new DataProcessFlow();
        approvalFlow2.setProcessId("TEST_APPROVAL");
        approvalFlow2.setFlowStep(FlowStep.STEP1);
        approvalFlow2.setFlowType("NORMAL");
        approvalFlow2.setApprovalUserType(ApprovalUserType.APPROVAL);
        approvalFlow2.setUser(new User("TEST_USER2"));
        approvalFlow2.setNextFlowStep(FlowStep.STEP2);
        dataProcessFlowService.save(approvalFlow2);

        DataProcessFlow approvalFlow3 = new DataProcessFlow();
        approvalFlow3.setProcessId("TEST_APPROVAL");
        approvalFlow3.setFlowStep(FlowStep.STEP2);
        approvalFlow3.setFlowType("NORMAL");
        approvalFlow3.setApprovalUserType(ApprovalUserType.AGREE);
        approvalFlow3.setUser(new User("TEST_USER3"));
        approvalFlow3.setNextFlowStep(FlowStep.FINAL);
        dataProcessFlowService.save(approvalFlow3);
    }

    @AfterEach
    public void delete() {
        dataProcessService.delete("TEST_APPROVAL");
    }

    @Test
    public void createInstanceByApprovalId() {
        //인스턴스를 만드는 방법 2가지
        // 1. 이미 정의된 approvalId에 대한 자동 생성
        // 2. 사용자 커스텀 생성

        DataProcessInstance instance = new DataProcessInstance();
        DataProcess approval = dataProcessService.getObject("TEST_APPROVAL");
        instance.setProcessId(approval.getProcessId());
        String instanceId = UUID.randomUUID().toString();
        instance.setInstanceId(instanceId);
        instance.setCurrentStep(FlowStep.STEP1);
        instance.setCurrentUser(new User("TEST_USER1"));
        instance.setStartDate(new Date());
        instance.setStatus(ApprovalStatus.CREATE);
        instance.setManageClass("com.favorsoft.mplatform.manager.dataprocess.BasicDataProcessManager");
        instance.setDataId("FIN_0000000004");
        instance.setDataType("MASTER");
        instance.setDataGroup("FINANCE");

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, approval.getValidDay());
        instance.setValidDate(cal.getTime());
        dataProcessInstanceService.save(instance);

        List<PropValue> propValueList = new ArrayList<>();
        PropValue propValue = new PropValue();
        propValue.setPropId("TEST_PROP2");
        propValue.setValue("TEST_EVENT_CODE1234");
        propValueList.add(propValue);

        Gson gson = new Gson();

        for(DataProcessFlow approvalFlow: approval.getApprovalFlowList()){
            DataProcessInstanceFlow instanceFlow = new DataProcessInstanceFlow();
            instanceFlow.setInstanceId(instanceId);
            instanceFlow.setFlowStep(approvalFlow.getFlowStep());
            instanceFlow.setNextFlowStep(approvalFlow.getNextFlowStep());
            instanceFlow.setUser(approvalFlow.getUser());
            instanceFlow.setData(gson.toJson(propValueList));
            instanceFlow.setApprovalUserType(approvalFlow.getApprovalUserType());
            dataProcessInstanceFlowService.save(instanceFlow);
        }
    }

    @Test
    public void createApproval() throws Exception {
        logger.info("##### CREATE APPROVAL BATCH #####");
        dataProcessBatchService.createDataProcess();
    }

    @Test
    public void processApproval() throws Exception {
        logger.info("##### PROCESS APPROVAL BATCH #####");
        List<DataProcessInstance> instanceList = dataProcessInstanceService.getList();
        DataProcessInstance instance = instanceList.stream().filter(s->s.getStatus().getApprovalStatus().equals("PROCESS")).findFirst().get();

        List<DataProcessInstanceFlow> instanceFlowList = instance.getApprovalInstanceFlowList();
        DataProcessInstanceFlow currentFlow = instanceFlowList.stream().filter(s->s.getFlowStep().equals(instance.getCurrentStep())).findFirst().get();
        currentFlow.setApprovalFlowStatus(ApprovalFlowStatus.ACCEPT); //승인 시나리오
        currentFlow.setProcessDate(new Date());
        dataProcessInstanceService.save(instance);

        dataProcessBatchService.activeDataProcess();
    }

    @Test
    public void finishApproval() throws Exception {
        logger.info("##### FINISH APPROVAL BATCH #####");
        dataProcessBatchService.finishDataProcess();
    }

}
