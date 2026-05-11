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
 * 每日统计汇总表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "daily_stats")
@Schema(name="daily_stats",description = "每日统计汇总表")
public class DailyStatsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit{

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
     * 统计日期
     */
    @Column(value = "stat_date")
    @Schema(description = "统计日期")
    private OffsetDateTime statDate;
    /**
     * 页面浏览量（PV）
     */
    @Column(value = "page_views")
    @Schema(description = "页面浏览量（PV）")
    private Long pageViews;
    /**
     * 独立访客数（UV）
     */
    @Column(value = "unique_visitors")
    @Schema(description = "独立访客数（UV）")
    private Long uniqueVisitors;
    /**
     * 文章阅读量
     */
    @Column(value = "post_reads")
    @Schema(description = "文章阅读量")
    private Long postReads;
    /**
     * 评论数
     */
    @Column(value = "comment_count")
    @Schema(description = "评论数")
    private Integer commentCount;
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

    /** 审计显示: 统计日期 */
    @Schema(description = "审计显示: 统计日期")
    public static final String SHOW_STATDATE = "statDate";

    /** 审计显示: 页面浏览量（PV） */
    @Schema(description = "审计显示: 页面浏览量（PV）")
    public static final String SHOW_PAGEVIEWS = "pageViews";

    /** 审计显示: 独立访客数（UV） */
    @Schema(description = "审计显示: 独立访客数（UV）")
    public static final String SHOW_UNIQUEVISITORS = "uniqueVisitors";

    /** 审计显示: 文章阅读量 */
    @Schema(description = "审计显示: 文章阅读量")
    public static final String SHOW_POSTREADS = "postReads";

    /** 审计显示: 评论数 */
    @Schema(description = "审计显示: 评论数")
    public static final String SHOW_COMMENTCOUNT = "commentCount";

    /** 审计显示: 创建时间 */
    @Schema(description = "审计显示: 创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

    /** 审计显示: 最后更新时间 */
    @Schema(description = "审计显示: 最后更新时间")
    public static final String SHOW_UPDATEDAT = "updatedAt";

}
