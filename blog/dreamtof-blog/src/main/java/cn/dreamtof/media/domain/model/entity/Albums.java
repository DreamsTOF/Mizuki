package cn.dreamtof.media.domain.model.entity;

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
 * 相册表 领域实体
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
@Schema(name="Albums", description = "相册表 领域实体")
public class Albums implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 相册唯一标识（目录名）
     */
     @Schema(description = "相册唯一标识（目录名）")
     private String albumKey;
    /**
     * 相册标题
     */
     @Schema(description = "相册标题")
     private String title;
    /**
     * 相册描述
     */
     @Schema(description = "相册描述")
     private String description;
    /**
     * 相册日期
     */
     @Schema(description = "相册日期")
     private OffsetDateTime date;
    /**
     * 拍摄地点
     */
     @Schema(description = "拍摄地点")
     private String location;
    /**
     * 标签列表
     */
     @Schema(description = "标签列表")
     private String tags;
    /**
     * 相册布局方式
     */
     @Schema(description = "相册布局方式")
     private String layout;
    /**
     * 展示列数
     */
     @Schema(description = "展示列数")
     private Integer columns;
    /**
     * 封面图片路径
     */
     @Schema(description = "封面图片路径")
     private String cover;
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