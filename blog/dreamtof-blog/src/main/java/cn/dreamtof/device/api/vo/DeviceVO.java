package cn.dreamtof.device.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DeviceVO", description = "设备响应对象")
public class DeviceVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "设备图片")
    private String image;

    @Schema(description = "设备规格参数")
    private String specs;

    @Schema(description = "设备描述")
    private String description;

    @Schema(description = "设备外部链接")
    private String link;

    @Schema(description = "所属分类名称")
    private String category;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
