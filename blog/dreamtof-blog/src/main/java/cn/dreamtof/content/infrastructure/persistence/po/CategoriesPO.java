package cn.dreamtof.content.infrastructure.persistence.po;

import cn.dreamtof.core.base.CreateTimeAudit;
import cn.dreamtof.core.base.UpdateTimeAudit;
import cn.dreamtof.core.base.VersionAudit;

import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;



/**
 * 文章分类表 领域实体
 * <p>
 * 职责：核心业务逻辑、领域行为校验、审计数据持有。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name="CategoriesPO", description = "文章分类表 领域实体")
public class CategoriesPO implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 分类名称
     */
     @Schema(description = "分类名称")
     private String name;
    /**
     * URL 友好的分类标识
     */
     @Schema(description = "URL 友好的分类标识")
     private String slug;
    /**
     * 分类描述
     */
     @Schema(description = "分类描述")
     private String description;
    /**
     * 父分类 ID，对应 categories.id
     */
     @Schema(description = "父分类 ID，对应 categories.id")
     private UUID parentId;
    /**
     * 分类图标
     */
     @Schema(description = "分类图标")
     private String icon;
    /**
     * 分类封面图片
     */
     @Schema(description = "分类封面图片")
     private String coverImage;
    /**
     * 排序顺序
     */
     @Schema(description = "排序顺序")
     private Integer sortOrder;
    /**
     * 是否启用
     */
     @Schema(description = "是否启用")
     private Boolean hasEnabled;
    /**
     * 乐观锁版本号
     */
     @Schema(description = "乐观锁版本号")
     private Integer version;
    /**
     * 创建时间
     */
     @Schema(description = "创建时间")
     private OffsetDateTime createdAt;
    /**
     * 最后更新时间
     */
     @Schema(description = "最后更新时间")
     private OffsetDateTime updatedAt;
    /**
     * 软删除时间戳
     */
     @Schema(description = "软删除时间戳")
     private OffsetDateTime deletedAt;

    // ==========================================
    // 🚀 领域行为 (Domain Logic)
    // ==========================================

    /**
     * 初始化业务逻辑
     */
    public void init() {
        // 在此处编写创建时的默认值或初始校验逻辑
    }

    /**
     * 业务校验：示例（如权限判断）
     */
    public boolean canBeManagedBy(Object userId) {
        // 利用实体已有的审计字段进行逻辑判断
        return true;
    }
}