package com.favorsoft.mplatform.cdn.domain;

import com.favorsoft.mplatform.cdn.domain.keys.DataProcessInstanceFlowKey;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalUserType;
import com.favorsoft.mplatform.cdn.enums.FlowStep;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(DataProcessInstanceFlowKey.class)
public class DataProcessInstanceFlow {
    @Id
    private String instanceId;

    @Id
    @Enumerated(EnumType.STRING)
    private FlowStep flowStep;

    @Enumerated(EnumType.STRING)
    private ApprovalUserType approvalUserType;

    @ManyToOne
    @JoinColumn(name= "userId", insertable = true, updatable = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ApprovalFlowStatus approvalFlowStatus;

    private String approvalMessage;

    private Date arriveDate;

    private Date processDate;

    @Enumerated(EnumType.STRING)
    private FlowStep nextFlowStep;

    @Lob
    private String data;
}
