package com.classification.domain_system.service.storage;

import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import io.minio.*;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageService implements FileStorageService {

    private final String minioUrl;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
    private MinioClient minioClient;

    public MinioStorageService(
            @Value("${minio.url}") String minioUrl,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName,
            @Value("${file.upload-dir}") String uploadDir) {
        this.minioUrl = minioUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;

        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to initialize MinioClient");
        }
    }

    private void ensureBucketExists() throws Exception {
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
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unknown_file";
        }
        String cleanName = StringUtils.cleanPath(originalFileName);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + originalFileName);
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
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Failed to store file via MinIO: " + originalFileName);
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        String cleanName = StringUtils.cleanPath(filename);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
        }

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
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Failed to load file from MinIO: " + filename);
        }
    }

    @Override
    public void deleteFile(String filename) {
        String cleanName = StringUtils.cleanPath(filename);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(cleanName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Failed to delete file from MinIO: " + filename);
        }
    }
}
