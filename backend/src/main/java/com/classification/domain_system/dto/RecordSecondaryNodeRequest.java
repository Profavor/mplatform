package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RecordSecondaryNodeRequest {
    /** 서브 노드 ID 목록 */
    private List<UUID> nodeIds;
}
