package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.favorsoft.mplatform.cdn.domain.keys.DataProcessFlowKey;

import com.favorsoft.mplatform.cdn.enums.ApprovalUserType;
import com.favorsoft.mplatform.cdn.enums.FlowStep;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(DataProcessFlowKey.class)
public class DataProcessFlow extends BaseEntity{
    @Id
    private String processId;

    @Id
    @Enumerated(EnumType.STRING)
    private FlowStep flowStep;

    private String flowType;

    @Enumerated(EnumType.STRING)
    private ApprovalUserType approvalUserType;

    @ManyToOne
    @JoinColumn(name= "userId", insertable = true, updatable = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private FlowStep nextFlowStep;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name= "processId", insertable = false, updatable = false)
    private DataProcess approvalObject;
}
