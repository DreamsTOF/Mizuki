package cn.dreamtof.media.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MusicTrackVO", description = "音乐曲目响应对象")
public class MusicTrackVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "所属播放列表ID")
    private UUID playlistId;
    @Schema(description = "曲目名称")
    private String title;
    @Schema(description = "艺术家/歌手")
    private String artist;
    @Schema(description = "专辑名称")
    private String album;
    @Schema(description = "封面图片")
    private String coverImage;
    @Schema(description = "音频文件URL")
    private String audioUrl;
    @Schema(description = "外部音频链接")
    private String externalUrl;
    @Schema(description = "歌词内容")
    private String lyrics;
    @Schema(description = "时长（秒）")
    private Integer duration;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
