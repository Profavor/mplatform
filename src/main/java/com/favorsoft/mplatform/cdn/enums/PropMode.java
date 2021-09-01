package com.favorsoft.mplatform.cdn.enums;

public enum PropMode {
    IDENTITY("IDENTITY"),
    NAME("NAME"),
    GENERAL("GENERAL");

    private String propMode;

    PropMode(String propMode){
        this.propMode = propMode;
    }

    public String getPropMode(){
        return propMode;
    }
}
