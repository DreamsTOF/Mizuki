package cn.dreamtof.system.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name = "SiteConfigs", description = "站点配置表 领域实体")
public class SiteConfigs implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    public static SiteConfigs create(String configKey, String configValue, String description) {
        Asserts.notBlank(configKey, "配置项键名不能为空");
        Asserts.notBlank(configValue, "配置项值不能为空");
        SiteConfigs entity = new SiteConfigs();
        entity.configKey = configKey;
        entity.configValue = configValue;
        entity.description = description;
        return entity;
    }

    public void updateValue(String configValue) {
        Asserts.notBlank(configValue, "配置项值不能为空");
        this.configValue = configValue;
    }
}
