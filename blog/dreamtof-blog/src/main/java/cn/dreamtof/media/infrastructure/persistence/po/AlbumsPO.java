package cn.dreamtof.media.infrastructure.persistence.po;

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
 * 相册表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "albums")
@Schema(name="albums",description = "相册表")
public class AlbumsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "album_key")
    @Schema(description = "相册唯一标识（目录名）")
    private String albumKey;

    @Column(value = "title")
    @Schema(description = "相册标题")
    private String title;

    @Column(value = "description")
    @Schema(description = "相册描述")
    private String description;

    @Column(value = "date")
    @Schema(description = "相册日期")
    private OffsetDateTime date;

    @Column(value = "location")
    @Schema(description = "拍摄地点")
    private String location;

    @Column(value = "tags", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "标签列表")
    private String tags;

    @Column(value = "layout")
    @Schema(description = "相册布局方式")
    private String layout;

    @Column(value = "columns")
    @Schema(description = "展示列数")
    private Integer columns;

    @Column(value = "cover")
    @Schema(description = "封面图片路径")
    private String cover;

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
    public static final String SHOW_ALBUMKEY = "albumKey";
    public static final String SHOW_TITLE = "title";
    public static final String SHOW_DESCRIPTION = "description";
    public static final String SHOW_DATE = "date";
    public static final String SHOW_LOCATION = "location";
    public static final String SHOW_TAGS = "tags";
    public static final String SHOW_LAYOUT = "layout";
    public static final String SHOW_COLUMNS = "columns";
    public static final String SHOW_COVER = "cover";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
