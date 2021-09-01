package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Approval extends BaseEntity{
    @Id
    @GeneratedValue
    private Long approvalId;
    private String approvalTitle;
    @Lob
    private String approvalBody;
    @Lob
    private String additionalHtmlContent;

    @ManyToOne
    @JoinColumn(name= "documentId", nullable = true)
    private Document document;

    private int currentSequence;

    @ManyToOne
    @JoinColumn(name="currentUserId", nullable = false)
    private User currentUser;

    private Date startDate;

    private Date endDate;

    private Date validDate;

    @JsonManagedReference
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "approval", fetch = FetchType.LAZY)
    private List<ApprovalFlow> approvalFlowList;

    private String refClass = "com.favorsoft.mplatform.manager.approval.BasicApprovalManager";

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String approvalType;
    private String notiRange;
    private Date reservationDate;
}
