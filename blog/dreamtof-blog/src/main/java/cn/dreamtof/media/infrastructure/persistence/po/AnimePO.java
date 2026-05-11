package cn.dreamtof.media.infrastructure.persistence.po;

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
 * 番剧表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "anime")
@Schema(name="anime",description = "番剧表")
public class AnimePO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 番剧标题
     */
    @Column(value = "title")
    @Schema(description = "番剧标题")
    private String title;
    /**
     * 观看状态
     */
    @Column(value = "status")
    @Schema(description = "观看状态")
    private String status;
    /**
     * 评分（0-10）
     */
    @Column(value = "rating")
    @Schema(description = "评分（0-10）")
    private BigDecimal rating;
    /**
     * 封面图片
     */
    @Column(value = "cover")
    @Schema(description = "封面图片")
    private String cover;
    /**
     * 番剧描述
     */
    @Column(value = "description")
    @Schema(description = "番剧描述")
    private String description;
    /**
     * 集数信息
     */
    @Column(value = "episodes")
    @Schema(description = "集数信息")
    private String episodes;
    /**
     * 年份
     */
    @Column(value = "year")
    @Schema(description = "年份")
    private String year;
    /**
     * 类型/流派数组
     */
    @Column(value = "genre", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "类型/流派数组")
    private String genre;
    /**
     * 制作公司
     */
    @Column(value = "studio")
    @Schema(description = "制作公司")
    private String studio;
    /**
     * 番剧链接
     */
    @Column(value = "link")
    @Schema(description = "番剧链接")
    private String link;
    /**
     * 当前观看进度
     */
    @Column(value = "progress")
    @Schema(description = "当前观看进度")
    private Integer progress;
    /**
     * 总集数
     */
    @Column(value = "total_episodes")
    @Schema(description = "总集数")
    private Integer totalEpisodes;
    /**
     * 开始观看日期
     */
    @Column(value = "start_date")
    @Schema(description = "开始观看日期")
    private String startDate;
    /**
     * 结束观看日期
     */
    @Column(value = "end_date")
    @Schema(description = "结束观看日期")
    private String endDate;
    /**
     * 排序顺序
     */
    @Column(value = "sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;
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
    /**
     * 软删除时间戳
     */
    @Column(value = "deleted_at", isLogicDelete = true)
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 番剧标题 */
    @Schema(description = "审计显示: 番剧标题")
    public static final String SHOW_TITLE = "title";

    /** 审计显示: 观看状态 */
    @Schema(description = "审计显示: 观看状态")
    public static final String SHOW_STATUS = "status";

    /** 审计显示: 评分（0-10） */
    @Schema(description = "审计显示: 评分（0-10）")
    public static final String SHOW_RATING = "rating";

    /** 审计显示: 封面图片 */
    @Schema(description = "审计显示: 封面图片")
    public static final String SHOW_COVER = "cover";

    /** 审计显示: 番剧描述 */
    @Schema(description = "审计显示: 番剧描述")
    public static final String SHOW_DESCRIPTION = "description";

    /** 审计显示: 集数信息 */
    @Schema(description = "审计显示: 集数信息")
    public static final String SHOW_EPISODES = "episodes";

    /** 审计显示: 年份 */
    @Schema(description = "审计显示: 年份")
    public static final String SHOW_YEAR = "year";

    /** 审计显示: 类型/流派数组 */
    @Schema(description = "审计显示: 类型/流派数组")
    public static final String SHOW_GENRE = "genre";

    /** 审计显示: 制作公司 */
    @Schema(description = "审计显示: 制作公司")
    public static final String SHOW_STUDIO = "studio";

    /** 审计显示: 番剧链接 */
    @Schema(description = "审计显示: 番剧链接")
    public static final String SHOW_LINK = "link";

    /** 审计显示: 当前观看进度 */
    @Schema(description = "审计显示: 当前观看进度")
    public static final String SHOW_PROGRESS = "progress";

    /** 审计显示: 总集数 */
    @Schema(description = "审计显示: 总集数")
    public static final String SHOW_TOTALEPISODES = "totalEpisodes";

    /** 审计显示: 开始观看日期 */
    @Schema(description = "审计显示: 开始观看日期")
    public static final String SHOW_STARTDATE = "startDate";

    /** 审计显示: 结束观看日期 */
    @Schema(description = "审计显示: 结束观看日期")
    public static final String SHOW_ENDDATE = "endDate";

    /** 审计显示: 排序顺序 */
    @Schema(description = "审计显示: 排序顺序")
    public static final String SHOW_SORTORDER = "sortOrder";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 创建时间 */
    @Schema(description = "审计显示: 创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

    /** 审计显示: 最后更新时间 */
    @Schema(description = "审计显示: 最后更新时间")
    public static final String SHOW_UPDATEDAT = "updatedAt";

    /** 审计显示: 软删除时间戳 */
    @Schema(description = "审计显示: 软删除时间戳")
    public static final String SHOW_DELETEDAT = "deletedAt";

}
