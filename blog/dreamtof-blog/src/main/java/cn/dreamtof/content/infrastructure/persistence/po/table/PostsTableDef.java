package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

public class PostsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final PostsTableDef POSTS_PO = new PostsTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn TITLE = new QueryColumn(this, "title");
    public final QueryColumn SLUG = new QueryColumn(this, "slug");
    public final QueryColumn CONTENT = new QueryColumn(this, "content");
    public final QueryColumn EXCERPT = new QueryColumn(this, "excerpt");
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");
    public final QueryColumn AUTHOR = new QueryColumn(this, "author");
    public final QueryColumn CATEGORY = new QueryColumn(this, "category");
    public final QueryColumn LANG = new QueryColumn(this, "lang");
    public final QueryColumn DRAFT = new QueryColumn(this, "draft");
    public final QueryColumn PINNED = new QueryColumn(this, "pinned");
    public final QueryColumn PRIORITY = new QueryColumn(this, "priority");
    public final QueryColumn ENCRYPTED = new QueryColumn(this, "encrypted");
    public final QueryColumn PASSWORD = new QueryColumn(this, "password");
    public final QueryColumn PASSWORD_HINT = new QueryColumn(this, "password_hint");
    public final QueryColumn ALIAS = new QueryColumn(this, "alias");
    public final QueryColumn PERMALINK = new QueryColumn(this, "permalink");
    public final QueryColumn LICENSE_NAME = new QueryColumn(this, "license_name");
    public final QueryColumn LICENSE_URL = new QueryColumn(this, "license_url");
    public final QueryColumn SOURCE_LINK = new QueryColumn(this, "source_link");
    public final QueryColumn IMAGE = new QueryColumn(this, "image");
    public final QueryColumn COMMENT = new QueryColumn(this, "comment");
    public final QueryColumn PUBLISHED = new QueryColumn(this, "published");
    public final QueryColumn VIEW_COUNT = new QueryColumn(this, "view_count");
    public final QueryColumn WORD_COUNT = new QueryColumn(this, "word_count");
    public final QueryColumn VERSION = new QueryColumn(this, "version");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
        ID, TITLE, SLUG, CONTENT, EXCERPT, DESCRIPTION, AUTHOR, CATEGORY, LANG,
        DRAFT, PINNED, PRIORITY, ENCRYPTED, PASSWORD, PASSWORD_HINT, ALIAS, PERMALINK,
        LICENSE_NAME, LICENSE_URL, SOURCE_LINK, IMAGE, COMMENT, PUBLISHED,
        VIEW_COUNT, WORD_COUNT, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT
    };

    public PostsTableDef() {
        super("public", "posts");
    }

    private PostsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public PostsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new PostsTableDef("public", "posts", alias));
    }
}
