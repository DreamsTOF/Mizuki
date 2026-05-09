package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 时间线链接关联表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TimelineEventLinksCursorReq", description = "时间线链接关联表游标分页请求")
public class TimelineEventLinksCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}