package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class Document extends BaseEntity {

    public Document(String documentId){
        this.documentId = documentId;
    }

    @Id
    private String documentId;

    @JsonManagedReference
    @OneToMany(cascade= CascadeType.ALL, orphanRemoval = true, mappedBy = "document", fetch = FetchType.EAGER)
    private List<FileAttach> fileAttachList = new ArrayList<>();

    private String reference;
}
