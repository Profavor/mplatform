package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.Document;
import com.favorsoft.mplatform.cdn.service.external.CommonService;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService extends CommonService<Document> {
     boolean createDocument(Document document, MultipartFile file, String fileId);
}
