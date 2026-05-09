package cn.dreamtof.content.domain.model.entity;

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
 * 文章归档索引表 领域实体
 * <p>
 * 职责：核心业务逻辑、领域行为校验、审计数据持有。
 * </p>
 *
 * @author dream
 * @since 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name="Archives", description = "文章归档索引表 领域实体")
public class Archives implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 归档年份
     */
     @Schema(description = "归档年份")
     private Integer year;
    /**
     * 归档月份
     */
     @Schema(description = "归档月份")
     private Integer month;
    /**
     * 该年月文章数量
     */
     @Schema(description = "该年月文章数量")
     private Integer postCount;
    /**
     * 该年月文章 ID 列表
     */
     @Schema(description = "该年月文章 ID 列表")
     private String postIds;
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