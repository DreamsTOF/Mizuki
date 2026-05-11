package cn.dreamtof.media.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 音乐曲目表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class MusicTracksTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 音乐曲目表
     */
    public static final MusicTracksTableDef MUSIC_TRACKS_PO = new MusicTracksTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 专辑名称
     */
    public final QueryColumn ALBUM = new QueryColumn(this, "album");

    /**
     * 曲目名称
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 艺术家/歌手
     */
    public final QueryColumn ARTIST = new QueryColumn(this, "artist");

    /**
     * 歌词内容
     */
    public final QueryColumn LYRICS = new QueryColumn(this, "lyrics");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 音频文件 URL
     */
    public final QueryColumn AUDIO_URL = new QueryColumn(this, "audio_url");

    /**
     * 时长（秒）
     */
    public final QueryColumn DURATION = new QueryColumn(this, "duration");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 封面图片
     */
    public final QueryColumn COVER_IMAGE = new QueryColumn(this, "cover_image");

    /**
     * 所属播放列表 ID，对应 music_playlists.id
     */
    public final QueryColumn PLAYLIST_ID = new QueryColumn(this, "playlist_id");

    /**
     * 外部音频链接
     */
    public final QueryColumn EXTERNAL_URL = new QueryColumn(this, "external_url");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PLAYLIST_ID, TITLE, ARTIST, ALBUM, COVER_IMAGE, AUDIO_URL, EXTERNAL_URL, LYRICS, DURATION, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public MusicTracksTableDef() {
        super("public", "music_tracks");
    }

    private MusicTracksTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public MusicTracksTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new MusicTracksTableDef("public", "music_tracks", alias));
    }

}
