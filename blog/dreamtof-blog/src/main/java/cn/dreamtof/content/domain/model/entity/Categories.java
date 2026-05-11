package cn.dreamtof.content.domain.model.entity;

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
@Schema(name = "Categories", description = "文章分类表 领域实体")
public class Categories implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "URL 友好的分类标识")
    private String slug;
    @Schema(description = "分类描述")
    private String description;
    @Schema(description = "父分类 ID")
    private UUID parentId;
    @Schema(description = "分类图标")
    private String icon;
    @Schema(description = "分类封面图片")
    private String coverImage;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
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

    public static Categories create(String name, String slug, UUID parentId,
                                    String description, String icon, String coverImage,
                                    Integer sortOrder, Boolean hasEnabled) {
        Asserts.notBlank(name, "分类名称不能为空");
        Asserts.notBlank(slug, "分类slug不能为空");

        Categories entity = new Categories();
        entity.name = name;
        entity.slug = slug;
        entity.parentId = parentId;
        entity.description = description;
        entity.icon = icon;
        entity.coverImage = coverImage;
        entity.sortOrder = sortOrder != null ? sortOrder : 0;
        entity.hasEnabled = hasEnabled != null ? hasEnabled : true;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String name, String slug, UUID parentId,
                       String description, String icon, String coverImage,
                       Integer sortOrder, Boolean hasEnabled) {
        if (name != null) {
            this.name = name;
        }
        if (slug != null) {
            this.slug = slug;
        }
        if (parentId != null) {
            this.parentId = parentId;
        }
        if (description != null) {
            this.description = description;
        }
        if (icon != null) {
            this.icon = icon;
        }
        if (coverImage != null) {
            this.coverImage = coverImage;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (hasEnabled != null) {
            this.hasEnabled = hasEnabled;
        }
    }

    public void markDeleted() {
        Asserts.isTrue(Asserts.isNull(this.deletedAt), "分类已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isRoot() {
        return this.parentId == null;
    }
}
