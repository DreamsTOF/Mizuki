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
@Schema(name = "AlbumVO", description = "相册响应对象")
public class AlbumVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "相册唯一标识")
    private String albumKey;

    @Schema(description = "相册标题")
    private String title;

    @Schema(description = "相册描述")
    private String description;

    @Schema(description = "相册日期")
    private String date;

    @Schema(description = "拍摄地点")
    private String location;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "相册布局方式")
    private String layout;

    @Schema(description = "展示列数")
    private Integer columns;

    @Schema(description = "封面图片路径")
    private String cover;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
