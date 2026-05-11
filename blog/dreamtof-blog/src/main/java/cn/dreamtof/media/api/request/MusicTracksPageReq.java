package cn.dreamtof.media.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 音乐曲目表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "MusicTracksPageReq", description = "音乐曲目表分页查询请求")
public class MusicTracksPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}