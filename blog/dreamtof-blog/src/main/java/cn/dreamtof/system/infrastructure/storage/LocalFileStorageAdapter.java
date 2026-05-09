package cn.dreamtof.system.infrastructure.storage;

import cn.dreamtof.core.exception.BusinessException;
import cn.dreamtof.system.domain.model.errorcode.FileUploadErrorCode;
import cn.dreamtof.system.domain.repository.FileStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class LocalFileStorageAdapter implements FileStoragePort {

    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public String store(InputStream inputStream, String folder, String storedName) {
        try {
            Path targetDir = Paths.get(basePath, folder);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedName);
            if (Files.exists(targetFile)) {
                throw new BusinessException(FileUploadErrorCode.UPLOAD_FAILED, "File already exists: " + storedName);
            }
            Files.copy(inputStream, targetFile);
            return targetFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new BusinessException(FileUploadErrorCode.UPLOAD_FAILED, e.getMessage());
        }
    }

    @Override
    public void removePhysical(String storagePath) {
        try {
            Path path = Paths.get(storagePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete physical file: {}, error: {}", storagePath, e.getMessage());
        }
    }

    @Override
    public String resolveUrl(String folder, String storedName) {
        return urlPrefix + "/" + folder + "/" + storedName;
    }
}
