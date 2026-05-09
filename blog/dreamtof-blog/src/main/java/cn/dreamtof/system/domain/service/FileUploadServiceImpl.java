package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.model.errorcode.FileUploadErrorCode;
import cn.dreamtof.system.domain.model.valueobject.FilePageQuery;
import cn.dreamtof.system.domain.model.valueobject.FileUploadContext;
import cn.dreamtof.system.domain.repository.FileStoragePort;
import cn.dreamtof.system.domain.repository.UploadedFilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    private static final java.util.Map<String, byte[]> MIME_MAGIC_BYTES = java.util.Map.of(
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8},
            "image/png", new byte[]{0x89, 0x50, 0x4E, 0x47},
            "image/gif", new byte[]{0x47, 0x49, 0x46, 0x38},
            "image/webp", new byte[]{0x52, 0x49, 0x46, 0x46},
            "image/svg+xml", new byte[]{0x3C},
            "image/x-icon", new byte[]{0x00, 0x00, 0x01, 0x00}
    );

    private final UploadedFilesRepository uploadedFilesRepository;
    private final FileStoragePort fileStoragePort;

    @Override
    public UploadedFiles upload(FileUploadContext context, UploadFolder folder) {
        Asserts.notNull(folder, FileUploadErrorCode.INVALID_FOLDER);
        Asserts.notNull(context, FileUploadErrorCode.INVALID_FILE_FORMAT);
        Asserts.notBlank(context.originalName(), FileUploadErrorCode.INVALID_FILE_FORMAT);

        byte[] fileBytes = readAllBytes(context.inputStream());
        validateExtension(context.originalName(), folder);
        validateMagicBytes(fileBytes, context.mimeType());
        validateSize(context.fileSize(), folder);

        String extension = extractExtension(context.originalName());
        String storedName = generateStoredName(extension);
        String storagePath = fileStoragePort.store(new ByteArrayInputStream(fileBytes), folder.getStorageSubPath(), storedName);
        String url = fileStoragePort.resolveUrl(folder.getStorageSubPath(), storedName);

        int[] dimensions = parseImageDimensions(fileBytes, context.mimeType());
        UploadedFiles entity = UploadedFiles.create(
                context.originalName(), storedName, storagePath, url,
                folder, context.fileSize(), context.mimeType(),
                dimensions[0], dimensions[1]
        );

        return uploadedFilesRepository.create(entity);
    }

    @Override
    public List<UploadedFiles> uploadBatch(List<FileUploadContext> files, UploadFolder folder) {
        Asserts.notNull(folder, FileUploadErrorCode.INVALID_FOLDER);
        Asserts.notEmpty(files, FileUploadErrorCode.INVALID_FILE_FORMAT);

        List<UploadedFiles> results = new ArrayList<>();
        for (FileUploadContext ctx : files) {
            try {
                UploadedFiles uploaded = upload(ctx, folder);
                results.add(uploaded);
            } catch (Exception e) {
                log.warn("Batch upload failed for file: {}, error: {}", ctx.originalName(), e.getMessage());
            }
        }
        return results;
    }

    @Override
    public void deleteById(UUID id) {
        UploadedFiles entity = uploadedFilesRepository.getById(id);
        Asserts.notNull(entity, FileUploadErrorCode.FILE_NOT_FOUND);

        uploadedFilesRepository.softDeleteById(id);
        removePhysicalFile(entity.getStoragePath());
    }

    @Override
    public void deleteByUrl(String url) {
        UploadedFiles entity = uploadedFilesRepository.findByUrl(url);
        Asserts.notNull(entity, FileUploadErrorCode.FILE_NOT_FOUND);

        uploadedFilesRepository.softDeleteById(entity.getId());
        removePhysicalFile(entity.getStoragePath());
    }

    @Override
    public PageResult<UploadedFiles> pageByFolder(FilePageQuery query) {
        return uploadedFilesRepository.page(query);
    }

    @Override
    public UploadedFiles findById(UUID id) {
        return uploadedFilesRepository.getById(id);
    }

    @Override
    public UploadedFiles findByUrl(String url) {
        return uploadedFilesRepository.findByUrl(url);
    }

    private void validateExtension(String originalName, UploadFolder folder) {
        String ext = extractExtension(originalName);
        Asserts.isTrue(folder.isExtensionAllowed(ext), FileUploadErrorCode.INVALID_FILE_FORMAT);
    }

    private void validateMagicBytes(byte[] fileBytes, String declaredMime) {
        if (declaredMime == null) return;
        byte[] magic = MIME_MAGIC_BYTES.get(declaredMime);
        if (magic == null) return;
        Asserts.isTrue(fileBytes.length >= magic.length, FileUploadErrorCode.MIME_MISMATCH);
        for (int i = 0; i < magic.length; i++) {
            Asserts.isTrue(fileBytes[i] == magic[i], FileUploadErrorCode.MIME_MISMATCH);
        }
    }

    private void validateSize(long fileSize, UploadFolder folder) {
        Asserts.isTrue(folder.isSizeAllowed(fileSize), FileUploadErrorCode.FILE_SIZE_EXCEEDED);
    }

    private String generateStoredName(String extension) {
        OffsetDateTime now = DateUtils.offsetNow();
        String datePart = now.format(DATE_FMT);
        String timePart = now.format(TIME_FMT);
        String uuid8 = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return datePart + "_" + timePart + "_" + uuid8 + "." + extension;
    }

    private String extractExtension(String originalName) {
        if (originalName == null) return "";
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx < 0 || dotIdx == originalName.length() - 1) return "";
        return originalName.substring(dotIdx + 1).toLowerCase();
    }

    private byte[] readAllBytes(InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new cn.dreamtof.core.exception.BusinessException(FileUploadErrorCode.UPLOAD_FAILED, e.getMessage());
        }
    }

    private int[] parseImageDimensions(byte[] fileBytes, String mimeType) {
        int[] dims = new int[2];
        if (mimeType == null || !mimeType.startsWith("image/") || "image/svg+xml".equals(mimeType)) {
            return dims;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            if (image != null) {
                dims[0] = image.getWidth();
                dims[1] = image.getHeight();
            }
        } catch (IOException e) {
            log.warn("Failed to parse image dimensions: {}", e.getMessage());
        }
        return dims;
    }

    private void removePhysicalFile(String storagePath) {
        try {
            fileStoragePort.removePhysical(storagePath);
        } catch (Exception e) {
            log.warn("Failed to remove physical file: {}, error: {}", storagePath, e.getMessage());
        }
    }
}
