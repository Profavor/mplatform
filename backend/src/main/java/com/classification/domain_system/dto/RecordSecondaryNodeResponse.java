package com.classification.domain_system.dto;

import com.classification.domain_system.entity.RecordSecondaryNode;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class RecordSecondaryNodeResponse {
    private final UUID id;
    private final UUID recordId;
    private final UUID nodeId;
    private final Map<String, String> nodeName;
    private final String nodePath;
    private final UUID axisId;
    private final String axisCode;
    private final Map<String, String> axisName;

    public static RecordSecondaryNodeResponse fromEntity(RecordSecondaryNode mapping) {
        if (mapping == null) return null;
        var node = mapping.getNode();
        var axis = node != null ? node.getAxis() : null;

        return RecordSecondaryNodeResponse.builder()
                .id(mapping.getId())
                .recordId(mapping.getRecordId())
                .nodeId(node != null ? node.getId() : null)
                .nodeName(node != null ? node.getName() : null)
                .nodePath(node != null ? node.getPath() : null)
                .axisId(mapping.getAxisId())
                .axisCode(axis != null ? axis.getAxisCode() : null)
                .axisName(axis != null ? axis.getName() : null)
                .build();
    }
}
