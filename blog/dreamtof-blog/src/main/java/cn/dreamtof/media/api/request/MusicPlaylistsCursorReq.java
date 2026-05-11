package cn.dreamtof.media.api.request;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 音乐播放列表表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "MusicPlaylistsCursorReq", description = "音乐播放列表表游标分页请求")
public class MusicPlaylistsCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}