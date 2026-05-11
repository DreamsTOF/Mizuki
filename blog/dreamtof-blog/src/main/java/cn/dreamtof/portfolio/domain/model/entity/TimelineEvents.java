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
@Schema(name = "TimelineEvents", description = "时间线事件表 领域实体")
public class TimelineEvents implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "事件标题")
    private String title;
    @Schema(description = "事件描述")
    private String description;
    @Schema(description = "事件类型")
    private String type;
    @Schema(description = "图标标识符")
    private String icon;
    @Schema(description = "颜色值")
    private String color;
    @Schema(description = "开始日期")
    private OffsetDateTime startDate;
    @Schema(description = "结束日期")
    private OffsetDateTime endDate;
    @Schema(description = "地点")
    private String location;
    @Schema(description = "所属机构")
    private String organization;
    @Schema(description = "职位/角色")
    private String position;
    @Schema(description = "是否重点展示")
    private Boolean featured;
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

    public static TimelineEvents create(String title, String description, String type,
                                        String icon, String color,
                                        OffsetDateTime startDate, OffsetDateTime endDate,
                                        String location, String organization, String position,
                                        Boolean featured) {
        Asserts.notBlank(title, "事件标题不能为空");
        TimelineEvents entity = new TimelineEvents();
        entity.title = title;
        entity.description = description;
        entity.type = type;
        entity.icon = icon;
        entity.color = color;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.location = location;
        entity.organization = organization;
        entity.position = position;
        entity.featured = featured != null ? featured : false;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String title, String description, String type,
                       String icon, String color,
                       OffsetDateTime startDate, OffsetDateTime endDate,
                       String location, String organization, String position,
                       Boolean featured) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (type != null) {
            this.type = type;
        }
        if (icon != null) {
            this.icon = icon;
        }
        if (color != null) {
            this.color = color;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (location != null) {
            this.location = location;
        }
        if (organization != null) {
            this.organization = organization;
        }
        if (position != null) {
            this.position = position;
        }
        if (featured != null) {
            this.featured = featured;
        }
    }

    public void markDeleted() {
        Asserts.isTrue(this.deletedAt == null, "时间线事件已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
