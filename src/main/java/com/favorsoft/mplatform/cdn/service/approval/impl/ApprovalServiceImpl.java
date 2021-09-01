package com.favorsoft.mplatform.cdn.service.approval.impl;

import com.favorsoft.mplatform.cdn.domain.Approval;
import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.Document;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.dto.ApprovalDTO;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.repository.jpa.ApprovalRepository;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;

    public ApprovalServiceImpl(ApprovalRepository approvalRepository){
        this.approvalRepository = approvalRepository;
    }

    @Override
    public List<Approval> getList() {
        return approvalRepository.findAll();
    }

    @Override
    public Approval save(Approval entity) {
        return approvalRepository.saveAndFlush(entity);
    }

    @Override
    public Approval getObject(Object key) {
        return approvalRepository.findById((Long) key).orElse(new Approval());
    }

    @Override
    public void delete(Object key) {
        approvalRepository.delete(getObject(key));
    }

    @Override
    public List<Approval> getList(ApprovalStatus status) {
        return approvalRepository.findByStatus(status);
    }

    @Override
    public List<Approval> getList(User user, ApprovalStatus status) {
        return approvalRepository.findByCurrentUserAndStatus(user, status);
    }

    @Override
    public Approval create(ApprovalDTO approvalDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Approval approval = new Approval();

        approval.setApprovalTitle(approvalDTO.getApprovalTitle());
        approval.setApprovalBody(approvalDTO.getApprovalBody());
        approval.setAdditionalHtmlContent(approvalDTO.getAdditionalHtmlContent());
        if(approvalDTO.getDocumentId() != null){
            approval.setDocument(new Document(approvalDTO.getDocumentId()));
        }

        approval.setCurrentSequence(0);
        approval.setCurrentUser(user);

        approval.setStatus(ApprovalStatus.CREATE);

        String route = approvalDTO.getRouteInfo();
        List<ApprovalFlow> approvalFlowList = new ArrayList();

        String routes[] = route.split("↔");
        for(String r: routes){
            String[] eachArr = r.split("↕");
            String seq = eachArr[0];
            String activity = eachArr[1];
            String userId = eachArr[2];

            ApprovalFlow approvalFlow = new ApprovalFlow();
            approvalFlow.setApproval(approval);
            // Only DRAFT
            if("0".equals(seq)){
                approvalFlow.setArriveDate(new Date());
                approvalFlow.setOpinion(approvalDTO.getApprComment());
            }

            approvalFlow.setApprovalFlowStatus("0".equals(seq) ? ApprovalFlowStatus.DRAFT : ApprovalFlowStatus.WAIT);
            approvalFlow.setUser("0".equals(seq) ? user: new User(userId));
            approvalFlow.setSequence(Integer.parseInt(seq));
            approvalFlow.setApprovalActivityCode(ApprovalActivityCode.find(activity));

            int size = approvalFlowList.size();

            if(size > 0){
                approvalFlowList.get(size - 1).setNextSequence(Integer.parseInt(seq));
            }

            approvalFlowList.add(approvalFlow);
        }
        approval.setApprovalFlowList(approvalFlowList);

        return save(approval);
    }
}
