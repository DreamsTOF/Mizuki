package cn.dreamtof.portfolio.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
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
@Schema(name = "TimelineEventAchievements", description = "时间线成就关联表 领域实体")
public class TimelineEventAchievements implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "关联的事件 ID")
    private UUID timelineEventId;
    @Schema(description = "成就描述")
    private String achievement;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "关联创建时间")
    private OffsetDateTime createdAt;

    public static TimelineEventAchievements create(UUID timelineEventId,
                                                   String achievement, Integer sortOrder) {
        Asserts.notNull(timelineEventId, "事件ID不能为空");
        Asserts.notBlank(achievement, "成就描述不能为空");
        TimelineEventAchievements entity = new TimelineEventAchievements();
        entity.timelineEventId = timelineEventId;
        entity.achievement = achievement;
        entity.sortOrder = sortOrder != null ? sortOrder : 0;
        return entity;
    }
}
