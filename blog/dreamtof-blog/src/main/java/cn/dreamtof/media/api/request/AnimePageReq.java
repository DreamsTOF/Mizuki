package cn.dreamtof.media.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 番剧表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AnimePageReq", description = "番剧表分页查询请求")
public class AnimePageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}