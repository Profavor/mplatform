package com.favorsoft.mplatform.cdn.domain.keys;

import com.favorsoft.mplatform.cdn.enums.FlowStep;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
public class DataProcessFlowKey implements Serializable {

    @JoinColumn(name = "approvalId")
    private String processId;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private FlowStep flowStep;

    public DataProcessFlowKey() {

    }

    public DataProcessFlowKey(String processId, FlowStep flowStep){
        this.processId = processId;
        this.flowStep = flowStep;
    }
}
