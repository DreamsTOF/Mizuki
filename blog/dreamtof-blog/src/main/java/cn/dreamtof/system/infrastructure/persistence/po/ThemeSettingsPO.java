package cn.dreamtof.system.infrastructure.persistence.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import cn.dreamtof.core.base.CreatedTimeAudit;
import cn.dreamtof.core.base.UpdatedTimeAudit;
import cn.dreamtof.core.base.VersionAudit;
import cn.dreamtof.core.base.CreatedByAudit;
import cn.dreamtof.core.base.UpdatedByAudit;
import cn.dreamtof.core.base.IdAudit;
import cn.dreamtof.common.persistence.handler.UniversalJsonTypeHandler;
import cn.dreamtof.common.persistence.handler.UUIDTypeHandler;
import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;





      

/**
 * 主题设置表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "theme_settings")
@Schema(name="theme_settings",description = "主题设置表")
public class ThemeSettingsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;
    /**
     * 设置项键名
     */
    @Column(value = "setting_key")
    @Schema(description = "设置项键名")
    private String settingKey;
    /**
     * 设置项值
     */
    @Column(value = "setting_value", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "设置项值")
    private String settingValue;
    /**
     * 设置项说明
     */
    @Column(value = "description")
    @Schema(description = "设置项说明")
    private String description;
    /**
     * 是否允许用户自定义
     */
    @Column(value = "has_user_customizable")
    @Schema(description = "是否允许用户自定义")
    private Boolean hasUserCustomizable;
    /**
     * 乐观锁版本号
     */
    @Column(value = "version")
    @Schema(description = "乐观锁版本号")
    private Integer version;
    /**
     * 创建时间
     */
    @Column(value = "created_at")
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    /**
     * 最后更新时间
     */
    @Column(value = "updated_at")
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 设置项键名 */
    @Schema(description = "审计显示: 设置项键名")
    public static final String SHOW_SETTINGKEY = "settingKey";

    /** 审计显示: 设置项值 */
    @Schema(description = "审计显示: 设置项值")
    public static final String SHOW_SETTINGVALUE = "settingValue";

    /** 审计显示: 设置项说明 */
    @Schema(description = "审计显示: 设置项说明")
    public static final String SHOW_DESCRIPTION = "description";

    /** 审计显示: 是否允许用户自定义 */
    @Schema(description = "审计显示: 是否允许用户自定义")
    public static final String SHOW_HASUSERCUSTOMIZABLE = "hasUserCustomizable";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 创建时间 */
    @Schema(description = "审计显示: 创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

    /** 审计显示: 最后更新时间 */
    @Schema(description = "审计显示: 最后更新时间")
    public static final String SHOW_UPDATEDAT = "updatedAt";

}
