package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.enums.FileErrorCode;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.repository.UploadedFilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileStorageProvider storageProvider;
    private final UploadedFilesRepository repository;

    public UploadedFiles upload(MultipartFile file, UploadFolder folder) {
        String extension = UploadFolder.extractExtension(file.getOriginalFilename());
        validateMagicNumber(file, extension);
        try {
            byte[] content = file.getBytes();
            String storedName = generateStoredNameFromEntity(file, folder);
            String storagePath = storageProvider.store(folder, storedName, content);
            String url = storageProvider.generateUrl(folder, storedName);
            int[] dim = parseImageDimension(content, folder, extension);
            String thumbnailUrl = null;
            if (dim != null) {
                thumbnailUrl = storageProvider.generateThumbnail(folder, storedName, content, dim[0], dim[1]);
            }
            int width = dim != null ? dim[0] : 0;
            int height = dim != null ? dim[1] : 0;
            UploadedFiles entity = UploadedFiles.create(file, folder, storagePath, url, width, height, thumbnailUrl);
            return repository.create(entity);
        } catch (IOException e) {
            log.error("文件上传IO异常: folder={}, originalName={}", folder.getPath(), file.getOriginalFilename(), e);
            throw new cn.dreamtof.core.exception.BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public List<UploadedFiles> batchUpload(List<MultipartFile> files, UploadFolder folder) {
        List<UploadedFiles> results = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            try {
                UploadedFiles uploaded = upload(file, folder);
                if (uploaded != null) {
                    results.add(uploaded);
                }
            } catch (Exception e) {
                log.warn("批量上传中单文件失败: originalName={}, error={}", file.getOriginalFilename(), e.getMessage());
            }
        }
        return results;
    }

    public boolean deleteFile(java.util.UUID fileId) {
        UploadedFiles entity = repository.getById(fileId);
        Asserts.notNull(entity, FileErrorCode.FILE_RECORD_NOT_FOUND);
        boolean physicalDeleted = storageProvider.deletePhysical(entity.getStoragePath());
        if (!physicalDeleted) {
            log.warn("物理文件删除失败，继续软删除DB记录: fileId={}", fileId);
        }
        return repository.removeById(fileId);
    }

    public PageResult<UploadedFiles> pageByFolder(String folder, PageReq pageReq) {
        return repository.pageByFolder(folder, pageReq);
    }

    private void validateMagicNumber(MultipartFile file, String extension) {
        byte[] expectedMagic = UploadFolder.getMagicNumber(extension);
        if (expectedMagic == null) {
            return;
        }
        try {
            byte[] header = new byte[expectedMagic.length];
            try (var is = file.getInputStream()) {
                int read = is.read(header);
                if (read < expectedMagic.length) {
                    Asserts.fail(FileErrorCode.FILE_MAGIC_MISMATCH);
                    return;
                }
            }
            for (int i = 0; i < expectedMagic.length; i++) {
                if (header[i] != expectedMagic[i]) {
                    Asserts.fail(FileErrorCode.FILE_MAGIC_MISMATCH);
                    return;
                }
            }
        } catch (IOException e) {
            log.error("Magic Number校验读取失败，拒绝上传: {}", e.getMessage());
            Asserts.fail(FileErrorCode.FILE_MAGIC_MISMATCH);
        }
    }

    private int[] parseImageDimension(byte[] content, UploadFolder folder, String extension) {
        if (!folder.isImageExtension(extension)) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
        } catch (IOException e) {
            log.warn("图片宽高解析失败: {}", e.getMessage());
        }
        return null;
    }

    private String generateStoredNameFromEntity(MultipartFile file, UploadFolder folder) {
        String extension = UploadFolder.extractExtension(file.getOriginalFilename());
        java.time.OffsetDateTime now = cn.dreamtof.core.utils.DateUtils.offsetNow();
        String datePart = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = now.format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        String uuid8 = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return datePart + "_" + timePart + "_" + uuid8 + "." + extension;
    }
}
