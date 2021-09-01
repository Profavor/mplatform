package com.favorsoft.mplatform.cdn.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FileAttach extends BaseEntity {
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name="documentId")
    private Document document;

    @Id
    private String fileId;
    private String fileName;
    private String fileOrgName;
    private long fileSize;

    @JsonIgnore
    private String filePath;

    private String status;
    private String fileType;
    private String serverInfo;

    private int totalDownCnt;
    private Date lastDownDate;
}
