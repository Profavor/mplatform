package com.classification.domain_system.service.storage;

import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;

@Service("minioStorageService")
@Primary
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final String minioUrl;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
    private MinioClient minioClient;
    private final FileValidationUtil fileValidationUtil;
    private final LocalStorageService localStorageService;

    public MinioStorageService(
            @Value("${minio.url:http://localhost:9000}") String minioUrl,
            @Value("${minio.access-key:minioadmin}") String accessKey,
            @Value("${minio.secret-key:minioadmin}") String secretKey,
            @Value("${minio.bucket-name:domain-system}") String bucketName,
            FileValidationUtil fileValidationUtil,
            LocalStorageService localStorageService) {
        this.minioUrl = minioUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
        this.fileValidationUtil = fileValidationUtil;
        this.localStorageService = localStorageService;

        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            log.warn("Could not connect to MinIO ({}), will fallback to local disk storage", minioUrl);
        }
    }

    private void ensureBucketExists() throws Exception {
        if (minioClient == null) return;
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public String storeFile(MultipartFile file) {
        String originalFileName = fileValidationUtil.sanitizeOrInferFilename(file.getOriginalFilename(), file.getContentType());
        String cleanName = StringUtils.cleanPath(originalFileName);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + originalFileName);
        }

        fileValidationUtil.validateExtension(cleanName);

        try (InputStream is = file.getInputStream()) {
            fileValidationUtil.validateMagicBytes(is);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Could not read file for validation");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = file.getBytes();
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String savedFileName = sb.toString() + getFileExtension(cleanName);

            if (minioClient != null) {
                try {
                    ensureBucketExists();
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(savedFileName)
                                    .stream(file.getInputStream(), file.getSize(), -1)
                                    .contentType(file.getContentType())
                                    .build()
                    );
                    return savedFileName;
                } catch (Exception minioEx) {
                    log.warn("MinIO upload failed ({}), falling back to local file storage: {}", minioEx.getMessage(), savedFileName);
                }
            }

            // Fallback to local storage
            return localStorageService.storeFile(file);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            log.error("Failed to store file, attempting local storage fallback:", e);
            return localStorageService.storeFile(file);
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        String cleanName = StringUtils.cleanPath(filename);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
        }

        if (minioClient != null) {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(cleanName)
                            .build())) {
                byte[] content = stream.readAllBytes();
                return new ByteArrayResource(content) {
                    @Override
                    public String getFilename() {
                        return cleanName;
                    }
                };
            } catch (Exception e) {
                log.debug("File not found in MinIO or MinIO unreachable ({}), trying local storage for: {}", e.getMessage(), cleanName);
            }
        }

        // Fallback to local disk storage
        return localStorageService.loadFileAsResource(cleanName);
    }

    @Override
    public void deleteFile(String filename) {
        String cleanName = StringUtils.cleanPath(filename);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
        }

        if (minioClient != null) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(cleanName)
                                .build()
                );
            } catch (Exception e) {
                log.debug("Failed to delete file from MinIO (or not present): {}", e.getMessage());
            }
        }

        try {
            localStorageService.deleteFile(cleanName);
        } catch (Exception ignored) {}
    }
}
