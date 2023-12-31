package com.favorsoft.mplatform.cdn.domain;

import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.enums.FlowStep;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DataProcessInstance extends BaseEntity{
    @Id
    private String instanceId;

    private String processId;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String dataType;   // [MASTER,CODE_GROUP]

    private String dataGroup;  // [DOMAIN_ID, CODE_GROUP_ID]

    private String dataId;     // [MASTER_ID, CODE]

    @Enumerated(EnumType.STRING)
    private FlowStep currentStep;

    @ManyToOne
    private User currentUser;

    private Date startDate;

    private Date endDate;

    private Date validDate;

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "instanceId", fetch = FetchType.EAGER)
    private List<DataProcessInstanceFlow> approvalInstanceFlowList;

    private String manageClass = "com.favorsoft.mplatform.manager.dataprocess.BasicDataProcessManager";
}
