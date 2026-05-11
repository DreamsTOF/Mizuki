package cn.dreamtof.system.domain.model.entity;

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
 * 页面访问统计表 领域实体
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
@Schema(name="PageViews", description = "页面访问统计表 领域实体")
public class PageViews implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 访问页面路径
     */
     @Schema(description = "访问页面路径")
     private String pagePath;
    /**
     * 页面类型
     */
     @Schema(description = "页面类型")
     private String pageType;
    /**
     * 关联的文章/页面 ID
     */
     @Schema(description = "关联的文章/页面 ID")
     private UUID targetId;
    /**
     * 访问者 IP
     */
     @Schema(description = "访问者 IP")
     private String ipAddress;
    /**
     * 访问者 User-Agent
     */
     @Schema(description = "访问者 User-Agent")
     private String userAgent;
    /**
     * 来源页面
     */
     @Schema(description = "来源页面")
     private String referer;
    /**
     * 访问时间
     */
     @Schema(description = "访问时间")
     private OffsetDateTime visitedAt;

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