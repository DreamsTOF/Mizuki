package cn.dreamtof.portfolio.infrastructure.persistence.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import cn.dreamtof.core.base.CreatedTimeAudit;
import cn.dreamtof.core.base.UpdatedTimeAudit;
import cn.dreamtof.core.base.VersionAudit;
import cn.dreamtof.core.base.IdAudit;
import cn.dreamtof.common.persistence.handler.UUIDTypeHandler;
import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

/**
 * 项目表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "projects")
@Schema(name="projects",description = "项目表")
public class ProjectsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "title")
    @Schema(description = "项目标题")
    private String title;

    @Column(value = "description")
    @Schema(description = "项目描述")
    private String description;

    @Column(value = "image")
    @Schema(description = "封面图片路径")
    private String image;

    @Column(value = "category")
    @Schema(description = "项目类别")
    private String category;

    @Column(value = "status")
    @Schema(description = "项目状态")
    private String status;

    @Column(value = "live_demo")
    @Schema(description = "在线演示地址")
    private String liveDemo;

    @Column(value = "source_code")
    @Schema(description = "源码仓库地址")
    private String sourceCode;

    @Column(value = "visit_url")
    @Schema(description = "项目主页地址")
    private String visitUrl;

    @Column(value = "start_date")
    @Schema(description = "开始日期")
    private OffsetDateTime startDate;

    @Column(value = "end_date")
    @Schema(description = "结束日期")
    private OffsetDateTime endDate;

    @Column(value = "featured")
    @Schema(description = "是否精选")
    private Boolean featured;

    @Column(value = "show_image")
    @Schema(description = "是否显示封面")
    private Boolean showImage;

    @Column(value = "sort_order")
    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Column(value = "version")
    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Column(value = "created_at")
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Column(value = "updated_at")
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    @Column(value = "deleted_at", isLogicDelete = true)
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    public static final String SHOW_ID = "id";
    public static final String SHOW_TITLE = "title";
    public static final String SHOW_DESCRIPTION = "description";
    public static final String SHOW_IMAGE = "image";
    public static final String SHOW_CATEGORY = "category";
    public static final String SHOW_STATUS = "status";
    public static final String SHOW_LIVEDEMO = "liveDemo";
    public static final String SHOW_SOURCECODE = "sourceCode";
    public static final String SHOW_VISITURL = "visitUrl";
    public static final String SHOW_STARTDATE = "startDate";
    public static final String SHOW_ENDDATE = "endDate";
    public static final String SHOW_FEATURED = "featured";
    public static final String SHOW_SHOWIMAGE = "showImage";
    public static final String SHOW_SORTORDER = "sortOrder";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
