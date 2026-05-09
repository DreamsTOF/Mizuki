package cn.dreamtof.media.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 相册图片表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class AlbumPhotosTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 相册图片表
     */
    public static final AlbumPhotosTableDef ALBUM_PHOTOS_PO = new AlbumPhotosTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 图片访问路径
     */
    public final QueryColumn URL = new QueryColumn(this, "url");

    /**
     * 文件大小
     */
    public final QueryColumn SIZE = new QueryColumn(this, "size");

    /**
     * 图片宽度
     */
    public final QueryColumn WIDTH = new QueryColumn(this, "width");

    /**
     * 图片高度
     */
    public final QueryColumn HEIGHT = new QueryColumn(this, "height");

    /**
     * 所属相册 ID，对应 albums.id
     */
    public final QueryColumn ALBUM_ID = new QueryColumn(this, "album_id");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 图片文件名
     */
    public final QueryColumn FILENAME = new QueryColumn(this, "filename");

    /**
     * 是否为封面
     */
    public final QueryColumn HAS_COVER = new QueryColumn(this, "has_cover");

    /**
     * MIME 类型
     */
    public final QueryColumn MIME_TYPE = new QueryColumn(this, "mime_type");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ALBUM_ID, FILENAME, URL, WIDTH, HEIGHT, SIZE, MIME_TYPE, HAS_COVER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public AlbumPhotosTableDef() {
        super("public", "album_photos");
    }

    private AlbumPhotosTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public AlbumPhotosTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new AlbumPhotosTableDef("public", "album_photos", alias));
    }

}
