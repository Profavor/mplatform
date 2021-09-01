package com.favorsoft.mplatform.cdn.domain.keys;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class PropEnumKey implements Serializable {
    @JoinColumn(name = "propId")
    private String propId;

    @Column(length = 100)
    private String code;

    public PropEnumKey(String propId, String code) {
        this.propId = propId;
        this.code = code;
    }
}
