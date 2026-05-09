package cn.dreamtof.portfolio.api.request;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 时间线成就关联表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TimelineEventAchievementsCursorReq", description = "时间线成就关联表游标分页请求")
public class TimelineEventAchievementsCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}