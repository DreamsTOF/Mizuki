package cn.dreamtof.system.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BannerVO", description = "横幅广告响应对象")
public class BannerVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "横幅标题")
    private String title;
    @Schema(description = "图片地址")
    private String imageUrl;
    @Schema(description = "设备类型")
    private String deviceType;
    @Schema(description = "展示位置")
    private String position;
    @Schema(description = "排序权重")
    private Integer sortOrder;
    @Schema(description = "是否轮播")
    private Boolean hasCarousel;
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
