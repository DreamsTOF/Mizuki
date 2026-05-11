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
@Schema(name = "DeviceCategoryVO", description = "设备分类响应对象")
public class DeviceCategoryVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "分类图标")
    private String icon;
    @Schema(description = "分类描述")
    private String description;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
