package cn.dreamtof.social.infrastructure.persistence.po;

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
 * 友链表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "friends")
@Schema(name="friends",description = "友链表")
public class FriendsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "title")
    @Schema(description = "友链网站标题")
    private String title;

    @Column(value = "desc")
    @Schema(description = "友链网站描述")
    private String desc;

    @Column(value = "siteurl")
    @Schema(description = "网站链接")
    private String siteurl;

    @Column(value = "imgurl")
    @Schema(description = "头像/Logo 图片 URL")
    private String imgurl;

    @Column(value = "img_type")
    @Schema(description = "图片类型：0=外链，1=本地")
    private Integer imgType;

    @Column(value = "img_storage_path")
    @Schema(description = "本地存储路径")
    private String imgStoragePath;

    @Column(value = "sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Column(value = "has_active")
    @Schema(description = "是否启用")
    private Boolean hasActive;

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
    public static final String SHOW_DESC = "desc";
    public static final String SHOW_SITEURL = "siteurl";
    public static final String SHOW_IMGURL = "imgurl";
    public static final String SHOW_IMGTYPE = "imgType";
    public static final String SHOW_IMGSTORAGEPATH = "imgStoragePath";
    public static final String SHOW_SORTORDER = "sortOrder";
    public static final String SHOW_HASACTIVE = "hasActive";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
