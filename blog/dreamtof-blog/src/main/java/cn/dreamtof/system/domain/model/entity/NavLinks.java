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
@Schema(name = "NavLinks", description = "导航链接表 领域实体")
public class NavLinks implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "链接名称")
    private String name;
    @Schema(description = "链接地址")
    private String url;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "是否外部链接")
    private Boolean hasExternal;
    @Schema(description = "是否新窗口打开")
    private Boolean hasNewWindow;
    @Schema(description = "父级链接 ID")
    private UUID parentId;
    @Schema(description = "导航位置")
    private String position;
    @Schema(description = "排序权重")
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

    public static NavLinks create(String name, String url, String icon,
                                  Boolean hasExternal, Boolean hasNewWindow,
                                  UUID parentId, String position, Integer sortOrder) {
        Asserts.notBlank(name, "导航链接名称不能为空");
        Asserts.notBlank(url, "导航链接地址不能为空");
        NavLinks entity = new NavLinks();
        entity.name = name;
        entity.url = url;
        entity.icon = icon;
        entity.hasExternal = hasExternal != null ? hasExternal : false;
        entity.hasNewWindow = hasNewWindow != null ? hasNewWindow : false;
        entity.parentId = parentId;
        entity.position = position;
        entity.sortOrder = sortOrder != null ? sortOrder : 0;
        entity.hasEnabled = true;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String name, String url, String icon,
                       Boolean hasExternal, Boolean hasNewWindow,
                       UUID parentId, String position, Integer sortOrder) {
        if (name != null) {
            this.name = name;
        }
        if (url != null) {
            this.url = url;
        }
        if (icon != null) {
            this.icon = icon;
        }
        if (hasExternal != null) {
            this.hasExternal = hasExternal;
        }
        if (hasNewWindow != null) {
            this.hasNewWindow = hasNewWindow;
        }
        if (parentId != null) {
            this.parentId = parentId;
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

    public void markDeleted() {
        Asserts.isTrue(this.deletedAt == null, "导航链接已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isRoot() {
        return this.parentId == null;
    }
}
