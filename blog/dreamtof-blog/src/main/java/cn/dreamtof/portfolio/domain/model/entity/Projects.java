package cn.dreamtof.portfolio.domain.model.entity;

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
@Schema(name = "Projects", description = "项目表 领域实体")
public class Projects implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "项目标题")
    private String title;
    @Schema(description = "项目描述")
    private String description;
    @Schema(description = "封面图片路径")
    private String image;
    @Schema(description = "项目类别")
    private String category;
    @Schema(description = "项目状态")
    private String status;
    @Schema(description = "在线演示地址")
    private String liveDemo;
    @Schema(description = "源码仓库地址")
    private String sourceCode;
    @Schema(description = "项目主页地址")
    private String visitUrl;
    @Schema(description = "开始日期")
    private OffsetDateTime startDate;
    @Schema(description = "结束日期")
    private OffsetDateTime endDate;
    @Schema(description = "是否精选")
    private Boolean featured;
    @Schema(description = "是否显示封面")
    private Boolean showImage;
    @Schema(description = "排序权重")
    private Integer sortOrder;
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

    public static Projects create(String title, String description, String image,
                                  String category, String status, String liveDemo,
                                  String sourceCode, String visitUrl,
                                  OffsetDateTime startDate, OffsetDateTime endDate,
                                  Boolean featured, Boolean showImage, Integer sortOrder) {
        Asserts.notBlank(title, "项目标题不能为空");
        Projects entity = new Projects();
        entity.title = title;
        entity.description = description;
        entity.image = image;
        entity.category = category;
        entity.status = status;
        entity.liveDemo = liveDemo;
        entity.sourceCode = sourceCode;
        entity.visitUrl = visitUrl;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.featured = featured != null ? featured : false;
        entity.showImage = showImage != null ? showImage : true;
        entity.sortOrder = sortOrder != null ? sortOrder : 0;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String title, String description, String image,
                       String category, String status, String liveDemo,
                       String sourceCode, String visitUrl,
                       OffsetDateTime startDate, OffsetDateTime endDate,
                       Boolean showImage, Integer sortOrder) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (image != null) {
            this.image = image;
        }
        if (category != null) {
            this.category = category;
        }
        if (status != null) {
            this.status = status;
        }
        if (liveDemo != null) {
            this.liveDemo = liveDemo;
        }
        if (sourceCode != null) {
            this.sourceCode = sourceCode;
        }
        if (visitUrl != null) {
            this.visitUrl = visitUrl;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (showImage != null) {
            this.showImage = showImage;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }

    public void toggleFeatured(boolean featured) {
        this.featured = featured;
    }

    public void markDeleted() {
        Asserts.isTrue(this.deletedAt == null, "项目已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
