package com.favorsoft.mplatform.cdn.enums;

public enum Language {
    KO("ko"), EN("en");
    private String language;

    Language(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }
}
