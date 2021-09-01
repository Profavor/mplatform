package com.favorsoft.mplatform.cdn.domain.keys;

import com.favorsoft.mplatform.cdn.enums.FlowStep;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
public class DataProcessInstanceFlowKey implements Serializable {
    @JoinColumn(table="approval_instance", name = "instanceId")
    private String instanceId;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private FlowStep flowStep;

    public DataProcessInstanceFlowKey(){

    }

    public DataProcessInstanceFlowKey(String instanceId, FlowStep flowStep){
        this.instanceId = instanceId;
        this.flowStep = flowStep;
    }

}
