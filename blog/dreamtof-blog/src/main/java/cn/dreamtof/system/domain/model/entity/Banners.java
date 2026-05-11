package cn.dreamtof.system.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name = "Banners", description = "横幅广告表 领域实体")
public class Banners implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "横幅标题")
    private String title;
    @Schema(description = "图片地址")
    private String imageUrl;
    @Schema(description = "设备类型")
    private String deviceType;
    @Schema(description = "展示位置")
    private String position;
    @Schema(description = "排序权重")
    private Integer sortOrder;
    @Schema(description = "是否轮播")
    private Boolean hasCarousel;
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static Banners create(String title, String imageUrl, String deviceType,
                                 String position, Integer sortOrder,
                                 Boolean hasCarousel) {
        Asserts.notBlank(title, "横幅标题不能为空");
        Asserts.notBlank(imageUrl, "图片地址不能为空");
        Banners entity = new Banners();
        entity.title = title;
        entity.imageUrl = imageUrl;
        entity.deviceType = deviceType;
        entity.position = position;
        entity.sortOrder = sortOrder != null ? sortOrder : 0;
        entity.hasCarousel = hasCarousel != null ? hasCarousel : false;
        entity.hasEnabled = true;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String title, String imageUrl, String deviceType,
                       String position, Integer sortOrder) {
        if (title != null) {
            this.title = title;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (deviceType != null) {
            this.deviceType = deviceType;
        }
        if (position != null) {
            this.position = position;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }

    public void toggleEnabled(boolean enabled) {
        this.hasEnabled = enabled;
    }

    public void toggleCarousel(boolean carousel) {
        this.hasCarousel = carousel;
    }

    public void markDeleted() {
        Asserts.isTrue(this.deletedAt == null, "横幅已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
