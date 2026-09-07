package com.duoq.medlearn.document.storage;

import com.duoq.medlearn.document.exception.DocumentProcessingException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@Slf4j
public class LocalFileStorageService {

    @Value("${app.document.upload-dir:uploads/documents}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(uploadDir));
        } catch (IOException e) {
            throw new DocumentProcessingException("Could not create upload dir: " + uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        var targetDir = Path.of(uploadDir);
        try {
            var filename = System.currentTimeMillis() + "_" + sanitizeFilename(file.getOriginalFilename());
            var targetPath = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    public String storeFromUrl(byte[] data, String sourceFilename) {
        var targetDir = Path.of(uploadDir);
        try {
            var filename = System.currentTimeMillis() + "_" + sanitizeFilename(sourceFilename);
            var targetPath = targetDir.resolve(filename);
            Files.write(targetPath, data);
            log.info("File stored from URL: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to store file from URL: " + sourceFilename, e);
        }
    }

    public byte[] read(String storagePath) {
        try {
            return Files.readAllBytes(Path.of(storagePath));
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read file: " + storagePath, e);
        }
    }

    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", storagePath, e);
        }
    }

    public String computeChecksum(MultipartFile file) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            try (var is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new DocumentProcessingException("Failed to compute checksum", e);
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
