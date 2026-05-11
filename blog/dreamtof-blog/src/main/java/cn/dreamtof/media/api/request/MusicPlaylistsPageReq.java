package cn.dreamtof.media.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 音乐播放列表表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "MusicPlaylistsPageReq", description = "音乐播放列表表分页查询请求")
public class MusicPlaylistsPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}