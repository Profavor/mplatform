package com.favorsoft.mplatform.cdn.domain;

import com.favorsoft.mplatform.cdn.domain.keys.PropEnumKey;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@IdClass(PropEnumKey.class)
public class PropEnum extends BaseEntity {
    @Id
    private String propId;

    @Id
    private String code;

    @NotNull
    private String value;

    private long dispSeq;

    @NonNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isEnable;

    @NonNull
    @Column(columnDefinition = "varchar(1) default 'N'")
    private String isDefault;
}
