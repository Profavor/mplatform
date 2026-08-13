package com.classification.domain_system.service.storage;

import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

@Service
@Primary
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements FileStorageService {

    private final Path fileStorageLocation;

    public LocalStorageService(@Value("${file.upload-dir}") String uploadDir) {
        Path location;
        try {
            location = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(location)) {
                Files.createDirectories(location);
            }
        } catch (Exception ex) {
            try {
                location = Paths.get(System.getProperty("java.io.tmpdir"), "uploads").toAbsolutePath().normalize();
                if (!Files.exists(location)) {
                    Files.createDirectories(location);
                }
            } catch (Exception e) {
                location = Paths.get("./uploads").toAbsolutePath().normalize();
            }
        }
        this.fileStorageLocation = location;
    }

    private String calculateHash(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] bytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(bytes)) != -1) {
                digest.update(bytes, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
            String hash = calculateHash(file);
            String extension = getFileExtension(cleanName);
            String savedFileName = hash + extension;

            Path targetLocation = this.fileStorageLocation.resolve(savedFileName);

            if (!Files.exists(targetLocation)) {
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return savedFileName;
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Could not store file " + originalFileName + ". Please try again!");
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        try {
            String cleanName = StringUtils.cleanPath(filename);
            if (cleanName.contains("..")) {
                throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
            }

            Path filePath = this.fileStorageLocation.resolve(cleanName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "File not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "File not found: " + filename);
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            String cleanName = StringUtils.cleanPath(filename);
            if (cleanName.contains("..")) {
                throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Filename contains invalid path sequence: " + filename);
            }

            Path filePath = this.fileStorageLocation.resolve(cleanName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FAIL, "Could not delete file: " + filename);
        }
    }
}
