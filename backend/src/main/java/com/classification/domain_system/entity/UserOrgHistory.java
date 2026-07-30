package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_org_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOrgHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "prev_organization_id")
    private UUID prevOrganizationId;

    @Column(name = "prev_department_id")
    private UUID prevDepartmentId;

    @Column(name = "prev_team_id")
    private UUID prevTeamId;

    @Column(name = "new_organization_id")
    private UUID newOrganizationId;

    @Column(name = "new_department_id")
    private UUID newDepartmentId;

    @Column(name = "new_team_id")
    private UUID newTeamId;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}
