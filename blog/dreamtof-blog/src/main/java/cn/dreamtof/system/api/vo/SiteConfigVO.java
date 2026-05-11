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
@Schema(name = "SiteConfigVO", description = "站点配置响应对象")
public class SiteConfigVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "配置项键名")
    private String configKey;

    @Schema(description = "配置项值")
    private String configValue;

    @Schema(description = "配置项说明")
    private String description;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
