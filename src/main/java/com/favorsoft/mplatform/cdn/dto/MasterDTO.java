package com.favorsoft.mplatform.cdn.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class MasterDTO {
    private String masterId;

    @NotNull
    private String domainId;

    @NotNull
    private String classId;

    private String status;

    private List<Map<String, Object>> classes;

    private List<String> props;

    private List<PropValue> data;

    private String lang = "ko";

    public MasterDTO(String masterId, List<PropValue> data) {
        this.masterId = masterId;
        this.data = data;
    }
}
