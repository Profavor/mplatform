package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DataProcess extends BaseEntity{
    @Id
    private String processId;

    @ManyToOne
    @JoinColumn(name="messageId")
    @RestResource(path = "message", rel="message", exported = false)
    private Message message;

    private String isEnable;

    private int validDay = 7; // Default 7 days

    @JsonManagedReference
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "approvalObject", fetch = FetchType.LAZY)
    private List<DataProcessFlow> approvalFlowList = new ArrayList<DataProcessFlow>();
}
