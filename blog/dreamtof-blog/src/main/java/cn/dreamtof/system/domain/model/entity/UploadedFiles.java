package cn.dreamtof.system.domain.model.entity;

import cn.dreamtof.core.base.IdAudit;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.model.errorcode.FileUploadErrorCode;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFiles implements Serializable, IdAudit {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String originalName;
    private String storedName;
    private String storagePath;
    private String url;
    private String folder;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    private String thumbnailUrl;
    private String metadata;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static UploadedFiles create(String originalName, String storedName, String storagePath,
                                       String url, UploadFolder folder, long fileSize,
                                       String mimeType, Integer width, Integer height) {
        Asserts.notBlank(originalName, FileUploadErrorCode.INVALID_FILE_FORMAT);
        Asserts.notBlank(storedName, FileUploadErrorCode.INVALID_FILE_FORMAT);

        UploadedFiles entity = new UploadedFiles();
        entity.originalName = originalName;
        entity.storedName = storedName;
        entity.storagePath = storagePath;
        entity.url = url;
        entity.folder = folder.getCode();
        entity.fileSize = fileSize;
        entity.mimeType = mimeType;
        entity.width = width;
        entity.height = height;
        return entity;
    }

    public void markDeleted() {
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
}
