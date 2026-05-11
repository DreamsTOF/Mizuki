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
@Schema(name = "ThemeSettingVO", description = "主题设置响应对象")
public class ThemeSettingVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "设置键")
    private String settingKey;
    @Schema(description = "设置值")
    private String settingValue;
    @Schema(description = "设置描述")
    private String description;
    @Schema(description = "是否允许用户自定义")
    private Boolean hasUserCustomizable;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
