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
    private final LocalStorageService fallbackStorage;
    private final Map<String, byte[]> mockInMemoryStore = new ConcurrentHashMap<>();

    private MinioClient minioClient;

    public MinioStorageService(
            @Value("${minio.url:http://localhost:9000}") String minioUrl,
            @Value("${minio.access-key:minioadmin}") String accessKey,
            @Value("${minio.secret-key:minioadmin}") String secretKey,
            @Value("${minio.bucket-name:domain-system}") String bucketName,
            @Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.minioUrl = minioUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
        this.fallbackStorage = new LocalStorageService(uploadDir);

        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            this.minioClient = null;
        }
    }

    private boolean isMinioAvailable() {
        if (minioClient == null) return false;
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
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

            if (isMinioAvailable()) {
                try {
                    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                    if (!found) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    }

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
                    // Fallback on connection error
                }
            }

            // Fallback / mock mode
            mockInMemoryStore.put(savedFileName, bytes);
            return fallbackStorage.storeFile(file);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Failed to store file via MinIO/Fallback: " + originalFileName);
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        String cleanName = StringUtils.cleanPath(filename);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
        }

        if (isMinioAvailable()) {
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
                // Fallback if not found in MinIO or error
            }
        }

        if (mockInMemoryStore.containsKey(cleanName)) {
            byte[] content = mockInMemoryStore.get(cleanName);
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return cleanName;
                }
            };
        }

        return fallbackStorage.loadFileAsResource(cleanName);
    }

    @Override
    public void deleteFile(String filename) {
        String cleanName = StringUtils.cleanPath(filename);
        if (cleanName.contains("..")) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
        }

        if (isMinioAvailable()) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(cleanName)
                                .build()
                );
            } catch (Exception e) {
                // Fallback
            }
        }

        mockInMemoryStore.remove(cleanName);
        fallbackStorage.deleteFile(cleanName);
    }
}
