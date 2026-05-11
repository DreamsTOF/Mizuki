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
@Schema(name = "TimelineEventSkills", description = "时间线技能关联表 领域实体")
public class TimelineEventSkills implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "关联的事件 ID")
    private UUID timelineEventId;
    @Schema(description = "技能名称")
    private String skillName;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "关联创建时间")
    private OffsetDateTime createdAt;

    public static TimelineEventSkills create(UUID timelineEventId, String skillName) {
        Asserts.notNull(timelineEventId, "事件ID不能为空");
        Asserts.notBlank(skillName, "技能名称不能为空");
        TimelineEventSkills entity = new TimelineEventSkills();
        entity.timelineEventId = timelineEventId;
        entity.skillName = skillName;
        return entity;
    }
}
