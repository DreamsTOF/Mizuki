package cn.dreamtof.system.domain.model.entity;

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
 * 搜索记录表 领域实体
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
@Schema(name="SearchLogs", description = "搜索记录表 领域实体")
public class SearchLogs implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 搜索关键词
     */
     @Schema(description = "搜索关键词")
     private String keyword;
    /**
     * 搜索结果数量
     */
     @Schema(description = "搜索结果数量")
     private Integer resultCount;
    /**
     * 搜索者 IP
     */
     @Schema(description = "搜索者 IP")
     private String ipAddress;
    /**
     * 搜索者 User-Agent
     */
     @Schema(description = "搜索者 User-Agent")
     private String userAgent;
    /**
     * 搜索时间
     */
     @Schema(description = "搜索时间")
     private OffsetDateTime searchedAt;

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