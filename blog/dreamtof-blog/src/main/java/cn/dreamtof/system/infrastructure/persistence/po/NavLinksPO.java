package cn.dreamtof.system.infrastructure.persistence.po;

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
 * 导航链接表 领域实体
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
@Schema(name="NavLinksPO", description = "导航链接表 领域实体")
public class NavLinksPO implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 链接名称
     */
     @Schema(description = "链接名称")
     private String name;
    /**
     * 链接 URL
     */
     @Schema(description = "链接 URL")
     private String url;
    /**
     * 图标名称
     */
     @Schema(description = "图标名称")
     private String icon;
    /**
     * 是否外部链接
     */
     @Schema(description = "是否外部链接")
     private Boolean hasExternal;
    /**
     * 是否新窗口打开
     */
     @Schema(description = "是否新窗口打开")
     private Boolean hasNewWindow;
    /**
     * 父链接 ID，对应 nav_links.id
     */
     @Schema(description = "父链接 ID，对应 nav_links.id")
     private UUID parentId;
    /**
     * 链接位置
     */
     @Schema(description = "链接位置")
     private String position;
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