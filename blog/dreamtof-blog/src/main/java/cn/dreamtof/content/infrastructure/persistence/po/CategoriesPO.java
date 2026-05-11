package cn.dreamtof.content.infrastructure.persistence.po;

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
 * 文章分类表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "categories")
@Schema(name="categories",description = "文章分类表")
public class CategoriesPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 分类名称
     */
    @Column(value = "name")
    @Schema(description = "分类名称")
    private String name;
    /**
     * URL 友好的分类标识
     */
    @Column(value = "slug")
    @Schema(description = "URL 友好的分类标识")
    private String slug;
    /**
     * 分类描述
     */
    @Column(value = "description")
    @Schema(description = "分类描述")
    private String description;
    /**
     * 父分类 ID，对应 categories.id
     */
    @Column(value = "parent_id")
    @Schema(description = "父分类 ID，对应 categories.id")
    private UUID parentId;
    /**
     * 分类图标
     */
    @Column(value = "icon")
    @Schema(description = "分类图标")
    private String icon;
    /**
     * 分类封面图片
     */
    @Column(value = "cover_image")
    @Schema(description = "分类封面图片")
    private String coverImage;
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

    /** 审计显示: 分类名称 */
    @Schema(description = "审计显示: 分类名称")
    public static final String SHOW_NAME = "name";

    /** 审计显示: URL 友好的分类标识 */
    @Schema(description = "审计显示: URL 友好的分类标识")
    public static final String SHOW_SLUG = "slug";

    /** 审计显示: 分类描述 */
    @Schema(description = "审计显示: 分类描述")
    public static final String SHOW_DESCRIPTION = "description";

    /** 审计显示: 父分类 ID，对应 categories.id */
    @Schema(description = "审计显示: 父分类 ID，对应 categories.id")
    public static final String SHOW_PARENTID = "parentId";

    /** 审计显示: 分类图标 */
    @Schema(description = "审计显示: 分类图标")
    public static final String SHOW_ICON = "icon";

    /** 审计显示: 分类封面图片 */
    @Schema(description = "审计显示: 分类封面图片")
    public static final String SHOW_COVERIMAGE = "coverImage";

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
