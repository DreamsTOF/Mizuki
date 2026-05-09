package cn.dreamtof.system.infrastructure.persistence.po;

import cn.dreamtof.core.base.IdAudit;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "uploaded_files")
public class UploadedFilesPO implements Serializable, IdAudit {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = "uuid_v7")
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
}
