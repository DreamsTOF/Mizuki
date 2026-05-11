package cn.dreamtof.content.infrastructure.persistence.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import cn.dreamtof.core.base.CreatedTimeAudit;
import cn.dreamtof.core.base.UpdatedTimeAudit;
import cn.dreamtof.core.base.VersionAudit;
import cn.dreamtof.core.base.IdAudit;
import cn.dreamtof.common.persistence.handler.UniversalJsonTypeHandler;
import cn.dreamtof.common.persistence.handler.UUIDTypeHandler;
import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

/**
 * 日记条目表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "diary_entries")
@Schema(name="diary_entries",description = "日记条目表")
public class DiaryEntriesPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "content")
    @Schema(description = "日记正文内容")
    private String content;

    @Column(value = "date")
    @Schema(description = "日记日期")
    private OffsetDateTime date;

    @Column(value = "images", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "图片 URL 数组")
    private String images;

    @Column(value = "location")
    @Schema(description = "地点信息")
    private String location;

    @Column(value = "mood")
    @Schema(description = "心情描述")
    private String mood;

    @Column(value = "tags", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "标签数组")
    private String tags;

    @Column(value = "version")
    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Column(value = "created_at")
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Column(value = "updated_at")
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    @Column(value = "deleted_at", isLogicDelete = true)
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    public static final String SHOW_ID = "id";
    public static final String SHOW_CONTENT = "content";
    public static final String SHOW_DATE = "date";
    public static final String SHOW_IMAGES = "images";
    public static final String SHOW_LOCATION = "location";
    public static final String SHOW_MOOD = "mood";
    public static final String SHOW_TAGS = "tags";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
