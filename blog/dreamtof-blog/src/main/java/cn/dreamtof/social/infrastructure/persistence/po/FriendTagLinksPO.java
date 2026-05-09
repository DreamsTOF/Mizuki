package cn.dreamtof.social.infrastructure.persistence.po;

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
 * 友链-标签关联表 实体类
 *
 * @author dream
 * @since 2026-05-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "friend_tag_links")
@Schema(name="friend_tag_links",description = "友链-标签关联表")
public class FriendTagLinksPO implements Serializable, IdAudit, CreatedTimeAudit, VersionAudit{

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
     * 关联的友链 ID，对应 friends.id
     */
    @Column(value = "friend_id")
    @Schema(description = "关联的友链 ID，对应 friends.id")
    private UUID friendId;
    /**
     * 关联的标签 ID，对应 friend_tags.id
     */
    @Column(value = "tag_id")
    @Schema(description = "关联的标签 ID，对应 friend_tags.id")
    private UUID tagId;
    /**
     * 乐观锁版本号
     */
    @Column(value = "version")
    @Schema(description = "乐观锁版本号")
    private Integer version;
    /**
     * 关联创建时间
     */
    @Column(value = "created_at")
    @Schema(description = "关联创建时间")
    private OffsetDateTime createdAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 关联的友链 ID，对应 friends.id */
    @Schema(description = "审计显示: 关联的友链 ID，对应 friends.id")
    public static final String SHOW_FRIENDID = "friendId";

    /** 审计显示: 关联的标签 ID，对应 friend_tags.id */
    @Schema(description = "审计显示: 关联的标签 ID，对应 friend_tags.id")
    public static final String SHOW_TAGID = "tagId";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 关联创建时间 */
    @Schema(description = "审计显示: 关联创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

}
