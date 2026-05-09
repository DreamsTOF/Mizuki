package cn.dreamtof.system.api.request;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 站点配置表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SiteConfigsCursorReq", description = "站点配置表游标分页请求")
public class SiteConfigsCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}