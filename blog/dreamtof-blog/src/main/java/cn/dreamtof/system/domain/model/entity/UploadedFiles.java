package cn.dreamtof.system.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.domain.model.enums.FileErrorCode;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name = "UploadedFiles", description = "文件上传记录表 领域实体")
public class UploadedFiles implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "存储文件名")
    private String storedName;

    @Schema(description = "存储路径")
    private String storagePath;

    @Schema(description = "可访问 URL")
    private String url;

    @Schema(description = "目标目录类型")
    private String folder;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "图片宽度")
    private Integer width;

    @Schema(description = "图片高度")
    private Integer height;

    @Schema(description = "缩略图 URL")
    private String thumbnailUrl;

    @Schema(description = "额外元数据")
    private String metadata;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    public static UploadedFiles create(MultipartFile file, UploadFolder folder,
                                       String storagePath, String url,
                                       int width, int height, String thumbnailUrl) {
        Asserts.isFalse(file == null || file.isEmpty(), FileErrorCode.FILE_EMPTY);
        String extension = UploadFolder.extractExtension(file.getOriginalFilename());
        Asserts.isTrue(folder.isExtensionAllowed(extension), FileErrorCode.FILE_EXTENSION_DENIED);
        Asserts.isTrue(folder.isSizeAllowed(file.getSize()), FileErrorCode.FILE_SIZE_EXCEEDED);
        Asserts.isTrue(folder.isMimeTypeAllowed(file.getContentType(), extension), FileErrorCode.FILE_MIME_MISMATCH);

        UploadedFiles entity = new UploadedFiles();
        entity.originalName = file.getOriginalFilename();
        entity.storedName = generateStoredName(extension);
        entity.storagePath = storagePath;
        entity.url = url;
        entity.folder = folder.getPath();
        entity.fileSize = file.getSize();
        entity.mimeType = file.getContentType();
        if (width > 0 && height > 0) {
            entity.width = width;
            entity.height = height;
        }
        entity.thumbnailUrl = thumbnailUrl;
        return entity;
    }

    public void markDeleted() {
        Asserts.isNull(this.deletedAt);
        this.deletedAt = DateUtils.offsetNow();
    }

    private static String generateStoredName(String extension) {
        OffsetDateTime now = DateUtils.offsetNow();
        String datePart = now.format(DATE_FMT);
        String timePart = now.format(TIME_FMT);
        String uuid8 = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return datePart + "_" + timePart + "_" + uuid8 + "." + extension;
    }
}
