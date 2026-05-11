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
 * 页面访问统计表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "page_views")
@Schema(name="page_views",description = "页面访问统计表")
public class PageViewsPO implements Serializable, IdAudit{

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
     * 访问页面路径
     */
    @Column(value = "page_path")
    @Schema(description = "访问页面路径")
    private String pagePath;
    /**
     * 页面类型
     */
    @Column(value = "page_type")
    @Schema(description = "页面类型")
    private String pageType;
    /**
     * 关联的文章/页面 ID
     */
    @Column(value = "target_id")
    @Schema(description = "关联的文章/页面 ID")
    private UUID targetId;
    /**
     * 访问者 IP
     */
    @Column(value = "ip_address")
    @Schema(description = "访问者 IP")
    private String ipAddress;
    /**
     * 访问者 User-Agent
     */
    @Column(value = "user_agent")
    @Schema(description = "访问者 User-Agent")
    private String userAgent;
    /**
     * 来源页面
     */
    @Column(value = "referer")
    @Schema(description = "来源页面")
    private String referer;
    /**
     * 访问时间
     */
    @Column(value = "visited_at")
    @Schema(description = "访问时间")
    private OffsetDateTime visitedAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 访问页面路径 */
    @Schema(description = "审计显示: 访问页面路径")
    public static final String SHOW_PAGEPATH = "pagePath";

    /** 审计显示: 页面类型 */
    @Schema(description = "审计显示: 页面类型")
    public static final String SHOW_PAGETYPE = "pageType";

    /** 审计显示: 关联的文章/页面 ID */
    @Schema(description = "审计显示: 关联的文章/页面 ID")
    public static final String SHOW_TARGETID = "targetId";

    /** 审计显示: 访问者 IP */
    @Schema(description = "审计显示: 访问者 IP")
    public static final String SHOW_IPADDRESS = "ipAddress";

    /** 审计显示: 访问者 User-Agent */
    @Schema(description = "审计显示: 访问者 User-Agent")
    public static final String SHOW_USERAGENT = "userAgent";

    /** 审计显示: 来源页面 */
    @Schema(description = "审计显示: 来源页面")
    public static final String SHOW_REFERER = "referer";

    /** 审计显示: 访问时间 */
    @Schema(description = "审计显示: 访问时间")
    public static final String SHOW_VISITEDAT = "visitedAt";

}
