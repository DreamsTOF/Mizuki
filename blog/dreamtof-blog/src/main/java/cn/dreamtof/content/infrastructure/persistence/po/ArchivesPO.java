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
 * 文章归档索引表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "archives")
@Schema(name="archives",description = "文章归档索引表")
public class ArchivesPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 归档年份
     */
    @Column(value = "year")
    @Schema(description = "归档年份")
    private Integer year;
    /**
     * 归档月份
     */
    @Column(value = "month")
    @Schema(description = "归档月份")
    private Integer month;
    /**
     * 该年月文章数量
     */
    @Column(value = "post_count")
    @Schema(description = "该年月文章数量")
    private Integer postCount;
    /**
     * 该年月文章 ID 列表
     */
    @Column(value = "post_ids", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "该年月文章 ID 列表")
    private String postIds;
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

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 归档年份 */
    @Schema(description = "审计显示: 归档年份")
    public static final String SHOW_YEAR = "year";

    /** 审计显示: 归档月份 */
    @Schema(description = "审计显示: 归档月份")
    public static final String SHOW_MONTH = "month";

    /** 审计显示: 该年月文章数量 */
    @Schema(description = "审计显示: 该年月文章数量")
    public static final String SHOW_POSTCOUNT = "postCount";

    /** 审计显示: 该年月文章 ID 列表 */
    @Schema(description = "审计显示: 该年月文章 ID 列表")
    public static final String SHOW_POSTIDS = "postIds";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 创建时间 */
    @Schema(description = "审计显示: 创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

    /** 审计显示: 最后更新时间 */
    @Schema(description = "审计显示: 最后更新时间")
    public static final String SHOW_UPDATEDAT = "updatedAt";

}
