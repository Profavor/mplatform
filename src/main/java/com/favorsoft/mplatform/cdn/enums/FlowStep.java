package com.favorsoft.mplatform.cdn.enums;

public enum FlowStep {
    DRAFT("DRAFT"),
    STEP1("STEP1"),
    STEP2("STEP2"),
    STEP3("STEP3"),
    STEP4("STEP4"),
    STEP5("STEP5"),
    STEP6("STEP6"),
    STEP7("STEP7"),
    STEP8("STEP8"),
    STEP9("STEP9"),
    FINAL("FINAL");

    private String flowStep;

    FlowStep(String flowStep){
        this.flowStep = flowStep;
    }

    public String getFlowStep(){
        return this.flowStep;
    }

}
