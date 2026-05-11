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
@Schema(name = "DeviceDetailVO", description = "设备详情响应对象")
public class DeviceDetailVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "设备名称")
    private String name;
    @Schema(description = "品牌")
    private String brand;
    @Schema(description = "型号")
    private String model;
    @Schema(description = "设备分类ID")
    private UUID categoryId;
    @Schema(description = "设备分类名称")
    private String categoryName;
    @Schema(description = "购买日期")
    private OffsetDateTime purchaseDate;
    @Schema(description = "价格")
    private String price;
    @Schema(description = "设备图片")
    private String image;
    @Schema(description = "设备描述")
    private String description;
    @Schema(description = "规格参数")
    private String specs;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
