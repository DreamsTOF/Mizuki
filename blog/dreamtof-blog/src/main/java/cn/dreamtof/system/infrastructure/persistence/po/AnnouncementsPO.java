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
 * 公告表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "announcements")
@Schema(name="announcements",description = "公告表")
public class AnnouncementsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 公告标题
     */
    @Column(value = "title")
    @Schema(description = "公告标题")
    private String title;
    /**
     * 公告内容
     */
    @Column(value = "content")
    @Schema(description = "公告内容")
    private String content;
    /**
     * 链接文本
     */
    @Column(value = "link_text")
    @Schema(description = "链接文本")
    private String linkText;
    /**
     * 链接 URL
     */
    @Column(value = "link_url")
    @Schema(description = "链接 URL")
    private String linkUrl;
    /**
     * 是否外部链接
     */
    @Column(value = "has_external_link")
    @Schema(description = "是否外部链接")
    private Boolean hasExternalLink;
    /**
     * 是否允许关闭
     */
    @Column(value = "has_closable")
    @Schema(description = "是否允许关闭")
    private Boolean hasClosable;
    /**
     * 是否启用
     */
    @Column(value = "has_enabled")
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    /**
     * 开始展示时间
     */
    @Column(value = "start_time")
    @Schema(description = "开始展示时间")
    private OffsetDateTime startTime;
    /**
     * 结束展示时间
     */
    @Column(value = "end_time")
    @Schema(description = "结束展示时间")
    private OffsetDateTime endTime;
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

    /** 审计显示: 公告标题 */
    @Schema(description = "审计显示: 公告标题")
    public static final String SHOW_TITLE = "title";

    /** 审计显示: 公告内容 */
    @Schema(description = "审计显示: 公告内容")
    public static final String SHOW_CONTENT = "content";

    /** 审计显示: 链接文本 */
    @Schema(description = "审计显示: 链接文本")
    public static final String SHOW_LINKTEXT = "linkText";

    /** 审计显示: 链接 URL */
    @Schema(description = "审计显示: 链接 URL")
    public static final String SHOW_LINKURL = "linkUrl";

    /** 审计显示: 是否外部链接 */
    @Schema(description = "审计显示: 是否外部链接")
    public static final String SHOW_HASEXTERNALLINK = "hasExternalLink";

    /** 审计显示: 是否允许关闭 */
    @Schema(description = "审计显示: 是否允许关闭")
    public static final String SHOW_HASCLOSABLE = "hasClosable";

    /** 审计显示: 是否启用 */
    @Schema(description = "审计显示: 是否启用")
    public static final String SHOW_HASENABLED = "hasEnabled";

    /** 审计显示: 开始展示时间 */
    @Schema(description = "审计显示: 开始展示时间")
    public static final String SHOW_STARTTIME = "startTime";

    /** 审计显示: 结束展示时间 */
    @Schema(description = "审计显示: 结束展示时间")
    public static final String SHOW_ENDTIME = "endTime";

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
