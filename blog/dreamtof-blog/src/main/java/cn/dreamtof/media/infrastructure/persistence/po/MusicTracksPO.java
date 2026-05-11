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
 * 音乐曲目表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "music_tracks")
@Schema(name="music_tracks",description = "音乐曲目表")
public class MusicTracksPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 所属播放列表 ID，对应 music_playlists.id
     */
    @Column(value = "playlist_id")
    @Schema(description = "所属播放列表 ID，对应 music_playlists.id")
    private UUID playlistId;
    /**
     * 曲目名称
     */
    @Column(value = "title")
    @Schema(description = "曲目名称")
    private String title;
    /**
     * 艺术家/歌手
     */
    @Column(value = "artist")
    @Schema(description = "艺术家/歌手")
    private String artist;
    /**
     * 专辑名称
     */
    @Column(value = "album")
    @Schema(description = "专辑名称")
    private String album;
    /**
     * 封面图片
     */
    @Column(value = "cover_image")
    @Schema(description = "封面图片")
    private String coverImage;
    /**
     * 音频文件 URL
     */
    @Column(value = "audio_url")
    @Schema(description = "音频文件 URL")
    private String audioUrl;
    /**
     * 外部音频链接
     */
    @Column(value = "external_url")
    @Schema(description = "外部音频链接")
    private String externalUrl;
    /**
     * 歌词内容
     */
    @Column(value = "lyrics")
    @Schema(description = "歌词内容")
    private String lyrics;
    /**
     * 时长（秒）
     */
    @Column(value = "duration")
    @Schema(description = "时长（秒）")
    private Integer duration;
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

    /** 审计显示: 所属播放列表 ID，对应 music_playlists.id */
    @Schema(description = "审计显示: 所属播放列表 ID，对应 music_playlists.id")
    public static final String SHOW_PLAYLISTID = "playlistId";

    /** 审计显示: 曲目名称 */
    @Schema(description = "审计显示: 曲目名称")
    public static final String SHOW_TITLE = "title";

    /** 审计显示: 艺术家/歌手 */
    @Schema(description = "审计显示: 艺术家/歌手")
    public static final String SHOW_ARTIST = "artist";

    /** 审计显示: 专辑名称 */
    @Schema(description = "审计显示: 专辑名称")
    public static final String SHOW_ALBUM = "album";

    /** 审计显示: 封面图片 */
    @Schema(description = "审计显示: 封面图片")
    public static final String SHOW_COVERIMAGE = "coverImage";

    /** 审计显示: 音频文件 URL */
    @Schema(description = "审计显示: 音频文件 URL")
    public static final String SHOW_AUDIOURL = "audioUrl";

    /** 审计显示: 外部音频链接 */
    @Schema(description = "审计显示: 外部音频链接")
    public static final String SHOW_EXTERNALURL = "externalUrl";

    /** 审计显示: 歌词内容 */
    @Schema(description = "审计显示: 歌词内容")
    public static final String SHOW_LYRICS = "lyrics";

    /** 审计显示: 时长（秒） */
    @Schema(description = "审计显示: 时长（秒）")
    public static final String SHOW_DURATION = "duration";

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
