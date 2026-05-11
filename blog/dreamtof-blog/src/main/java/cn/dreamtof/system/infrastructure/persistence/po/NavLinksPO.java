package cn.dreamtof.system.infrastructure.persistence.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import cn.dreamtof.core.base.CreatedTimeAudit;
import cn.dreamtof.core.base.UpdatedTimeAudit;
import cn.dreamtof.core.base.VersionAudit;
import cn.dreamtof.core.base.CreatedByAudit;
import cn.dreamtof.core.base.UpdatedByAudit;
import cn.dreamtof.core.base.IdAudit;
import cn.dreamtof.common.persistence.handler.UniversalJsonTypeHandler;
import cn.dreamtof.common.persistence.handler.UUIDTypeHandler;
import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;





      

/**
 * 导航链接表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "nav_links")
@Schema(name="nav_links",description = "导航链接表")
public class NavLinksPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;
    /**
     * 链接名称
     */
    @Column(value = "name")
    @Schema(description = "链接名称")
    private String name;
    /**
     * 链接 URL
     */
    @Column(value = "url")
    @Schema(description = "链接 URL")
    private String url;
    /**
     * 图标名称
     */
    @Column(value = "icon")
    @Schema(description = "图标名称")
    private String icon;
    /**
     * 是否外部链接
     */
    @Column(value = "has_external")
    @Schema(description = "是否外部链接")
    private Boolean hasExternal;
    /**
     * 是否新窗口打开
     */
    @Column(value = "has_new_window")
    @Schema(description = "是否新窗口打开")
    private Boolean hasNewWindow;
    /**
     * 父链接 ID，对应 nav_links.id
     */
    @Column(value = "parent_id")
    @Schema(description = "父链接 ID，对应 nav_links.id")
    private UUID parentId;
    /**
     * 链接位置
     */
    @Column(value = "position")
    @Schema(description = "链接位置")
    private String position;
    /**
     * 排序顺序
     */
    @Column(value = "sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    /**
     * 是否启用
     */
    @Column(value = "has_enabled")
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    /**
     * 乐观锁版本号
     */
    @Column(value = "version")
    @Schema(description = "乐观锁版本号")
    private Integer version;
    /**
     * 创建时间
     */
    @Column(value = "created_at")
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    /**
     * 最后更新时间
     */
    @Column(value = "updated_at")
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;
    /**
     * 软删除时间戳
     */
    @Column(value = "deleted_at", isLogicDelete = true)
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 链接名称 */
    @Schema(description = "审计显示: 链接名称")
    public static final String SHOW_NAME = "name";

    /** 审计显示: 链接 URL */
    @Schema(description = "审计显示: 链接 URL")
    public static final String SHOW_URL = "url";

    /** 审计显示: 图标名称 */
    @Schema(description = "审计显示: 图标名称")
    public static final String SHOW_ICON = "icon";

    /** 审计显示: 是否外部链接 */
    @Schema(description = "审计显示: 是否外部链接")
    public static final String SHOW_HASEXTERNAL = "hasExternal";

    /** 审计显示: 是否新窗口打开 */
    @Schema(description = "审计显示: 是否新窗口打开")
    public static final String SHOW_HASNEWWINDOW = "hasNewWindow";

    /** 审计显示: 父链接 ID，对应 nav_links.id */
    @Schema(description = "审计显示: 父链接 ID，对应 nav_links.id")
    public static final String SHOW_PARENTID = "parentId";

    /** 审计显示: 链接位置 */
    @Schema(description = "审计显示: 链接位置")
    public static final String SHOW_POSITION = "position";

    /** 审计显示: 排序顺序 */
    @Schema(description = "审计显示: 排序顺序")
    public static final String SHOW_SORTORDER = "sortOrder";

    /** 审计显示: 是否启用 */
    @Schema(description = "审计显示: 是否启用")
    public static final String SHOW_HASENABLED = "hasEnabled";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 创建时间 */
    @Schema(description = "审计显示: 创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

    /** 审计显示: 最后更新时间 */
    @Schema(description = "审计显示: 最后更新时间")
    public static final String SHOW_UPDATEDAT = "updatedAt";

    /** 审计显示: 软删除时间戳 */
    @Schema(description = "审计显示: 软删除时间戳")
    public static final String SHOW_DELETEDAT = "deletedAt";

}
