package cn.dreamtof.media.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 音乐播放列表表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class MusicPlaylistsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 音乐播放列表表
     */
    public static final MusicPlaylistsTableDef MUSIC_PLAYLISTS_PO = new MusicPlaylistsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 播放列表名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

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
     * 是否启用
     */
    public final QueryColumn HAS_ENABLED = new QueryColumn(this, "has_enabled");

    /**
     * 播放列表描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, DESCRIPTION, COVER_IMAGE, SORT_ORDER, HAS_ENABLED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public MusicPlaylistsTableDef() {
        super("public", "music_playlists");
    }

    private MusicPlaylistsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public MusicPlaylistsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new MusicPlaylistsTableDef("public", "music_playlists", alias));
    }

}
