package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 文件上传记录表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class UploadedFilesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件上传记录表
     */
    public static final UploadedFilesTableDef UPLOADED_FILES_PO = new UploadedFilesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 可访问 URL
     */
    public final QueryColumn URL = new QueryColumn(this, "url");

    /**
     * 图片宽度
     */
    public final QueryColumn WIDTH = new QueryColumn(this, "width");

    /**
     * 目标目录类型
     */
    public final QueryColumn FOLDER = new QueryColumn(this, "folder");

    /**
     * 图片高度
     */
    public final QueryColumn HEIGHT = new QueryColumn(this, "height");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 文件大小
     */
    public final QueryColumn FILE_SIZE = new QueryColumn(this, "file_size");

    /**
     * 额外元数据
     */
    public final QueryColumn METADATA = new QueryColumn(this, "metadata");

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
     * 存储文件名
     */
    public final QueryColumn STORED_NAME = new QueryColumn(this, "stored_name");

    /**
     * 存储路径
     */
    public final QueryColumn STORAGE_PATH = new QueryColumn(this, "storage_path");

    /**
     * 原始文件名
     */
    public final QueryColumn ORIGINAL_NAME = new QueryColumn(this, "original_name");

    /**
     * 缩略图 URL
     */
    public final QueryColumn THUMBNAIL_URL = new QueryColumn(this, "thumbnail_url");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ORIGINAL_NAME, STORED_NAME, STORAGE_PATH, URL, FOLDER, FILE_SIZE, MIME_TYPE, WIDTH, HEIGHT, THUMBNAIL_URL, METADATA, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public UploadedFilesTableDef() {
        super("public", "uploaded_files");
    }

    private UploadedFilesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public UploadedFilesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new UploadedFilesTableDef("public", "uploaded_files", alias));
    }

}
