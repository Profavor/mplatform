package com.classification.domain_system.service.storage;

import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FileValidationUtil {

    private final Set<String> allowedExtensions;
    private static final List<String> BLACKLIST_EXTENSIONS = Arrays.asList("html", "htm", "svg", "js", "mjs", "xhtml", "shtml");

    public FileValidationUtil(@Value("${file.upload.allowed-extensions:jpg,jpeg,png,gif,pdf,xlsx,xls,csv,docx,doc,pptx,ppt,txt,zip}") String allowedExtensionsStr) {
        this.allowedExtensions = Arrays.stream(allowedExtensionsStr.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public void validateExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "File extension is missing or invalid");
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        if (BLACKLIST_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Upload of this file type is restricted: ." + extension);
        }
        
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "File extension not allowed: ." + extension);
        }
    }

    public void validateMagicBytes(InputStream inputStream) {
        try {
            // Read first 512 bytes
            byte[] headerBytes = new byte[512];
            int bytesRead = inputStream.read(headerBytes);
            if (bytesRead > 0) {
                String headerStr = new String(headerBytes, 0, bytesRead, StandardCharsets.UTF_8).toLowerCase();
                // Basic XSS patterns
                if (headerStr.contains("<script") || headerStr.contains("<html") || headerStr.contains("<svg")) {
                    throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "File content contains restricted signature (possible XSS attempt)");
                }
            }
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Failed to validate file content signature");
        }
    }
    
    public static boolean isDangerousExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return BLACKLIST_EXTENSIONS.contains(extension);
    }
}
