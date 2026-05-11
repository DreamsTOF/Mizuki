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
 * 时间线事件表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "timeline_events")
@Schema(name="timeline_events",description = "时间线事件表")
public class TimelineEventsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "title")
    @Schema(description = "事件标题")
    private String title;

    @Column(value = "description")
    @Schema(description = "事件描述")
    private String description;

    @Column(value = "type")
    @Schema(description = "事件类型")
    private String type;

    @Column(value = "icon")
    @Schema(description = "图标标识符")
    private String icon;

    @Column(value = "color")
    @Schema(description = "颜色值")
    private String color;

    @Column(value = "start_date")
    @Schema(description = "开始日期")
    private OffsetDateTime startDate;

    @Column(value = "end_date")
    @Schema(description = "结束日期")
    private OffsetDateTime endDate;

    @Column(value = "location")
    @Schema(description = "地点")
    private String location;

    @Column(value = "organization")
    @Schema(description = "所属机构")
    private String organization;

    @Column(value = "position")
    @Schema(description = "职位/角色")
    private String position;

    @Column(value = "featured")
    @Schema(description = "是否重点展示")
    private Boolean featured;

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
    public static final String SHOW_TYPE = "type";
    public static final String SHOW_ICON = "icon";
    public static final String SHOW_COLOR = "color";
    public static final String SHOW_STARTDATE = "startDate";
    public static final String SHOW_ENDDATE = "endDate";
    public static final String SHOW_LOCATION = "location";
    public static final String SHOW_ORGANIZATION = "organization";
    public static final String SHOW_POSITION = "position";
    public static final String SHOW_FEATURED = "featured";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
