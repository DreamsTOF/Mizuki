package cn.dreamtof.content.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 文章主表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PostsPageReq", description = "文章主表分页查询请求")
public class PostsPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}