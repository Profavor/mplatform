package com.favorsoft.mplatform.cdn.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentDTO {
    private String documentId;
    private String fileId;
    private MultipartFile file;
}
