package com.classification.domain_system.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Stores the uploaded file safely and returns the unique saved filename/path.
     */
    String storeFile(MultipartFile file);

    /**
     * Loads the stored file as a Resource by filename.
     */
    Resource loadFileAsResource(String filename);

    /**
     * Deletes the stored file by filename.
     */
    void deleteFile(String filename);
}
