package cn.dreamtof.content.infrastructure.persistence.po;

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
 * 文章-标签关联表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "post_tags")
@Schema(name="post_tags",description = "文章-标签关联表")
public class PostTagsPO implements Serializable, IdAudit, CreatedTimeAudit, VersionAudit{

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
     * 关联的文章 ID，对应 posts.id
     */
    @Column(value = "post_id")
    @Schema(description = "关联的文章 ID，对应 posts.id")
    private UUID postId;
    /**
     * 关联的标签 ID，对应 tags.id
     */
    @Column(value = "tag_id")
    @Schema(description = "关联的标签 ID，对应 tags.id")
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

    /** 审计显示: 关联的文章 ID，对应 posts.id */
    @Schema(description = "审计显示: 关联的文章 ID，对应 posts.id")
    public static final String SHOW_POSTID = "postId";

    /** 审计显示: 关联的标签 ID，对应 tags.id */
    @Schema(description = "审计显示: 关联的标签 ID，对应 tags.id")
    public static final String SHOW_TAGID = "tagId";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 关联创建时间 */
    @Schema(description = "审计显示: 关联创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

}
