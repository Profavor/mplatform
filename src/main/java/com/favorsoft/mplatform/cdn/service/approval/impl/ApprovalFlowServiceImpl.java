package com.favorsoft.mplatform.cdn.service.approval.impl;

import com.favorsoft.mplatform.cdn.domain.ApprovalFlow;
import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import com.favorsoft.mplatform.cdn.repository.jpa.ApprovalFlowRepository;
import com.favorsoft.mplatform.cdn.service.approval.ApprovalFlowService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalFlowServiceImpl implements ApprovalFlowService {

    private final ApprovalFlowRepository approvalFlowRepository;

    public ApprovalFlowServiceImpl(ApprovalFlowRepository approvalFlowRepository){
        this.approvalFlowRepository = approvalFlowRepository;
    }

    @Override
    public List<ApprovalFlow> getList() {
        return approvalFlowRepository.findAll();
    }

    @Override
    public ApprovalFlow save(ApprovalFlow entity) {
        return approvalFlowRepository.saveAndFlush(entity);
    }

    @Override
    public ApprovalFlow getObject(Object key) {
        return approvalFlowRepository.findById((Long) key).orElse(new ApprovalFlow());
    }

    @Override
    public void delete(Object key) {
        approvalFlowRepository.delete(getObject(key));
    }

    @Override
    public List<ApprovalFlow> getList(User user, ApprovalActivityCode approvalActivityCode) {
        return approvalFlowRepository.findByUserAndApprovalActivityCode(user, approvalActivityCode);
    }
}
