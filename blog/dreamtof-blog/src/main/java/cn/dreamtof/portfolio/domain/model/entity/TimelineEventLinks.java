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
@Schema(name = "TimelineEventLinks", description = "时间线链接关联表 领域实体")
public class TimelineEventLinks implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "关联的事件 ID")
    private UUID timelineEventId;
    @Schema(description = "链接名称")
    private String name;
    @Schema(description = "链接地址")
    private String url;
    @Schema(description = "链接类型")
    private String linkType;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "关联创建时间")
    private OffsetDateTime createdAt;

    public static TimelineEventLinks create(UUID timelineEventId, String name,
                                            String url, String linkType) {
        Asserts.notNull(timelineEventId, "事件ID不能为空");
        Asserts.notBlank(url, "链接地址不能为空");
        TimelineEventLinks entity = new TimelineEventLinks();
        entity.timelineEventId = timelineEventId;
        entity.name = name;
        entity.url = url;
        entity.linkType = linkType;
        return entity;
    }
}
