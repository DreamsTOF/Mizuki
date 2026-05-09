package cn.dreamtof.social.api.request;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 评论表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentsCursorReq", description = "评论表游标分页请求")
public class CommentsCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}