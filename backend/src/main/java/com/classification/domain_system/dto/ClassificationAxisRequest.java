package com.classification.domain_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ClassificationAxisRequest {
    private String axisCode;
    private Map<String, String> name;
    private String description;
    private Boolean isDefault;
    private Integer sortOrder;

    @JsonProperty("code")
    public void setCode(String code) {
        if (this.axisCode == null || this.axisCode.isEmpty()) {
            this.axisCode = code;
        }
    }

    public String getCode() {
        return this.axisCode;
    }

    @JsonSetter("name")
    public void setNameFromJackson(Object nameObj) {
        if (nameObj instanceof String str) {
            this.name = Map.of("ko", str, "en", str);
        } else if (nameObj instanceof Map<?, ?> map) {
            Map<String, String> res = new HashMap<>();
            map.forEach((k, v) -> res.put(String.valueOf(k), String.valueOf(v)));
            this.name = res;
        }
    }
}
