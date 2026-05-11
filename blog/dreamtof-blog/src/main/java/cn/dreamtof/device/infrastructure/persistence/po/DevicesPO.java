package cn.dreamtof.device.infrastructure.persistence.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import cn.dreamtof.core.base.CreatedTimeAudit;
import cn.dreamtof.core.base.UpdatedTimeAudit;
import cn.dreamtof.core.base.VersionAudit;
import cn.dreamtof.core.base.IdAudit;
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
 * 设备表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "devices")
@Schema(name="devices",description = "设备表")
public class DevicesPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "category_id")
    @Schema(description = "所属分类 ID，对应 device_categories.id")
    private UUID categoryId;

    @Column(value = "name")
    @Schema(description = "设备名称")
    private String name;

    @Column(value = "image")
    @Schema(description = "设备图片")
    private String image;

    @Column(value = "specs")
    @Schema(description = "设备规格参数")
    private String specs;

    @Column(value = "description")
    @Schema(description = "设备描述")
    private String description;

    @Column(value = "link")
    @Schema(description = "设备外部链接")
    private String link;

    @Column(value = "sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;

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
    public static final String SHOW_CATEGORYID = "categoryId";
    public static final String SHOW_NAME = "name";
    public static final String SHOW_IMAGE = "image";
    public static final String SHOW_SPECS = "specs";
    public static final String SHOW_DESCRIPTION = "description";
    public static final String SHOW_LINK = "link";
    public static final String SHOW_SORTORDER = "sortOrder";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
