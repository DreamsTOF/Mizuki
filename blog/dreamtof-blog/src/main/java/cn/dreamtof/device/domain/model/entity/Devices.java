package cn.dreamtof.device.domain.model.entity;

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
 * 设备表 领域实体
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
@Schema(name="Devices", description = "设备表 领域实体")
public class Devices implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 所属分类 ID，对应 device_categories.id
     */
     @Schema(description = "所属分类 ID，对应 device_categories.id")
     private UUID categoryId;
    /**
     * 设备名称
     */
     @Schema(description = "设备名称")
     private String name;
    /**
     * 设备图片
     */
     @Schema(description = "设备图片")
     private String image;
    /**
     * 设备规格参数
     */
     @Schema(description = "设备规格参数")
     private String specs;
    /**
     * 设备描述
     */
     @Schema(description = "设备描述")
     private String description;
    /**
     * 设备外部链接
     */
     @Schema(description = "设备外部链接")
     private String link;
    /**
     * 排序顺序
     */
     @Schema(description = "排序顺序")
     private Integer sortOrder;
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