package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.favorsoft.mplatform.cdn.enums.ApprovalActivityCode;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import lombok.Data;

import java.util.Date;
import jakarta.persistence.*;

@Entity
@Data
public class ApprovalFlow extends BaseEntity{

    @Id
    @GeneratedValue
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name= "approvalId", nullable = false)
    private Approval approval;

    private int sequence;
    private int nextSequence;

    @Enumerated(EnumType.STRING)
    private ApprovalActivityCode approvalActivityCode; //기안(0), 결재자(1), 합의자(2), 병력합의자(4), 병렬결재자(7), 통보자(9)

    @ManyToOne
    @JoinColumn(name= "userId", nullable = false)
    private User user;

    private Date arriveDate;

    private Date processDate;

    @Enumerated(EnumType.STRING)
    private ApprovalFlowStatus approvalFlowStatus;   //기안, 승인, 반려, 대기

    private String opinion; //의견


}
