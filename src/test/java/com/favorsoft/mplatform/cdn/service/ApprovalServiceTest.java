package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.batch.ApprovalBatchService;
import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.Document;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalService;
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

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@ActiveProfiles("local")
public class ApprovalServiceTest {
    private static Logger logger = LoggerFactory.getLogger(ApprovalServiceTest.class);

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ApprovalBatchService approvalBatchService;

    @BeforeEach
    public void create() {
        logger.info("##### Spring security TEST_ACCOUNT 인증 #####");
        User user = new User("TEST_USER1");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        Approval approval = new Approval();

        approval.setApprovalTitle("Title text");
        approval.setApprovalBody("<div>내앞에 모여진 수많은 길</div>");
        approval.setAdditionalHtmlContent("<table><tr><td>안녕</td></tr></table>");
        approval.setDocument(new Document("1a13f044-fd71-4348-8123-1c67226e2c02"));
        approval.setCurrentSequence(0);
        approval.setCurrentUser(new User("TEST_USER2"));

        approval.setStatus(ApprovalStatus.CREATE);

        List<ApprovalFlow> approvalFlowList = new ArrayList<ApprovalFlow>();
        ApprovalFlow approvalFlow1 = new ApprovalFlow();
        approvalFlow1.setApproval(approval);
        approvalFlow1.setApprovalFlowStatus(ApprovalFlowStatus.DRAFT);
        approvalFlow1.setSequence(0);
        approvalFlow1.setNextSequence(1);
        approvalFlow1.setUser(new User("TEST_USER2"));
        approvalFlow1.setOpinion("결재 승인 부탁드립니다.");
        approvalFlow1.setApprovalActivityCode(ApprovalActivityCode.DRAFT);
        approvalFlowList.add(approvalFlow1);

        ApprovalFlow approvalFlow2 = new ApprovalFlow();
        approvalFlow2.setApproval(approval);
        approvalFlow2.setApprovalFlowStatus(ApprovalFlowStatus.WAIT);
        approvalFlow2.setSequence(1);
        approvalFlow2.setNextSequence(2);
        approvalFlow2.setUser(new User("TEST_USER1"));
        approvalFlow2.setApprovalActivityCode(ApprovalActivityCode.APPROVAL);
        approvalFlowList.add(approvalFlow2);

        ApprovalFlow approvalFlow3 = new ApprovalFlow();
        approvalFlow3.setApproval(approval);
        approvalFlow3.setApprovalFlowStatus(ApprovalFlowStatus.WAIT);
        approvalFlow3.setSequence(2);
        approvalFlow3.setUser(new User("TEST_USER3"));
        approvalFlow3.setApprovalActivityCode(ApprovalActivityCode.APPROVAL);
        approvalFlowList.add(approvalFlow3);

        approval.setApprovalFlowList(approvalFlowList);

        approvalService.save(approval);
    }

    @Test
    public void createApprovalId() throws Exception {
        approvalBatchService.createApproval();
    }

    @Test
    public void activeApproval() throws Exception {
        approvalBatchService.activeApproval();
    }



    @AfterEach
    public void delete() {

    }
}
