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
 * 搜索记录表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "search_logs")
@Schema(name="search_logs",description = "搜索记录表")
public class SearchLogsPO implements Serializable, IdAudit{

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
     * 搜索关键词
     */
    @Column(value = "keyword")
    @Schema(description = "搜索关键词")
    private String keyword;
    /**
     * 搜索结果数量
     */
    @Column(value = "result_count")
    @Schema(description = "搜索结果数量")
    private Integer resultCount;
    /**
     * 搜索者 IP
     */
    @Column(value = "ip_address")
    @Schema(description = "搜索者 IP")
    private String ipAddress;
    /**
     * 搜索者 User-Agent
     */
    @Column(value = "user_agent")
    @Schema(description = "搜索者 User-Agent")
    private String userAgent;
    /**
     * 搜索时间
     */
    @Column(value = "searched_at")
    @Schema(description = "搜索时间")
    private OffsetDateTime searchedAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 搜索关键词 */
    @Schema(description = "审计显示: 搜索关键词")
    public static final String SHOW_KEYWORD = "keyword";

    /** 审计显示: 搜索结果数量 */
    @Schema(description = "审计显示: 搜索结果数量")
    public static final String SHOW_RESULTCOUNT = "resultCount";

    /** 审计显示: 搜索者 IP */
    @Schema(description = "审计显示: 搜索者 IP")
    public static final String SHOW_IPADDRESS = "ipAddress";

    /** 审计显示: 搜索者 User-Agent */
    @Schema(description = "审计显示: 搜索者 User-Agent")
    public static final String SHOW_USERAGENT = "userAgent";

    /** 审计显示: 搜索时间 */
    @Schema(description = "审计显示: 搜索时间")
    public static final String SHOW_SEARCHEDAT = "searchedAt";

}
