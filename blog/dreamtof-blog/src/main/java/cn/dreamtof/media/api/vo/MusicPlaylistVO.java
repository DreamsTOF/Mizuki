package cn.dreamtof.media.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MusicPlaylistVO", description = "音乐播放列表响应对象")
public class MusicPlaylistVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "播放列表名称")
    private String name;
    @Schema(description = "播放列表描述")
    private String description;
    @Schema(description = "封面图片")
    private String coverImage;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
