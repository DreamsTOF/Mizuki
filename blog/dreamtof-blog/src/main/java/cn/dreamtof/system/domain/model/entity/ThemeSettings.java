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
@Schema(name = "ThemeSettings", description = "主题设置表 领域实体")
public class ThemeSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static ThemeSettings create(String settingKey, String settingValue,
                                       String description, Boolean hasUserCustomizable) {
        Asserts.notBlank(settingKey, "设置键不能为空");
        ThemeSettings entity = new ThemeSettings();
        entity.settingKey = settingKey;
        entity.settingValue = settingValue;
        entity.description = description;
        entity.hasUserCustomizable = hasUserCustomizable != null ? hasUserCustomizable : false;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String settingValue, String description, Boolean hasUserCustomizable) {
        if (settingValue != null) {
            this.settingValue = settingValue;
        }
        if (description != null) {
            this.description = description;
        }
        if (hasUserCustomizable != null) {
            this.hasUserCustomizable = hasUserCustomizable;
        }
    }

    public void updateValue(String settingValue) {
        Asserts.notBlank(settingValue, "设置值不能为空");
        this.settingValue = settingValue;
    }
}
