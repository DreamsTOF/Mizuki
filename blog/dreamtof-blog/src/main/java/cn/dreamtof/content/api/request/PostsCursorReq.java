package cn.dreamtof.content.api.request;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 文章主表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PostsCursorReq", description = "文章主表游标分页请求")
public class PostsCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}