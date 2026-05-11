package cn.dreamtof.content.infrastructure.persistence.po;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "posts")
@Schema(name="posts",description = "文章主表")
public class PostsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;

    @Column(value = "title")
    @Schema(description = "文章标题")
    private String title;

    @Column(value = "slug")
    @Schema(description = "URL 友好标识符")
    private String slug;

    @Column(value = "content")
    @Schema(description = "Markdown 正文内容")
    private String content;

    @Column(value = "excerpt")
    @Schema(description = "自动生成的摘要")
    private String excerpt;

    @Column(value = "description")
    @Schema(description = "文章描述/SEO 摘要")
    private String description;

    @Column(value = "author")
    @Schema(description = "作者名")
    private String author;

    @Column(value = "category")
    @Schema(description = "分类名称")
    private String category;

    @Column(value = "lang")
    @Schema(description = "语言代码")
    private String lang;

    @Column(value = "draft")
    @Schema(description = "是否为草稿状态")
    private Boolean draft;

    @Column(value = "pinned")
    @Schema(description = "是否置顶")
    private Boolean pinned;

    @Column(value = "priority")
    @Schema(description = "置顶优先级")
    private Integer priority;

    @Column(value = "encrypted")
    @Schema(description = "是否加密")
    private Boolean encrypted;

    @Column(value = "password")
    @Schema(description = "加密密码哈希")
    private String password;

    @Column(value = "password_hint")
    @Schema(description = "密码提示信息")
    private String passwordHint;

    @Column(value = "alias")
    @Schema(description = "文章别名")
    private String alias;

    @Column(value = "permalink")
    @Schema(description = "自定义固定链接")
    private String permalink;

    @Column(value = "license_name")
    @Schema(description = "许可证名称")
    private String licenseName;

    @Column(value = "license_url")
    @Schema(description = "许可证链接")
    private String licenseUrl;

    @Column(value = "source_link")
    @Schema(description = "原文链接")
    private String sourceLink;

    @Column(value = "image")
    @Schema(description = "封面图片路径")
    private String image;

    @Column(value = "comment")
    @Schema(description = "是否启用评论")
    private Boolean comment;

    @Column(value = "published")
    @Schema(description = "发布日期时间")
    private OffsetDateTime published;

    @Column(value = "view_count")
    @Schema(description = "浏览次数")
    private Long viewCount;

    @Column(value = "word_count")
    @Schema(description = "字数统计")
    private Integer wordCount;

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
    public static final String SHOW_SLUG = "slug";
    public static final String SHOW_CONTENT = "content";
    public static final String SHOW_EXCERPT = "excerpt";
    public static final String SHOW_DESCRIPTION = "description";
    public static final String SHOW_AUTHOR = "author";
    public static final String SHOW_CATEGORY = "category";
    public static final String SHOW_LANG = "lang";
    public static final String SHOW_DRAFT = "draft";
    public static final String SHOW_PINNED = "pinned";
    public static final String SHOW_PRIORITY = "priority";
    public static final String SHOW_ENCRYPTED = "encrypted";
    public static final String SHOW_PASSWORD = "password";
    public static final String SHOW_PASSWORDHINT = "passwordHint";
    public static final String SHOW_ALIAS = "alias";
    public static final String SHOW_PERMALINK = "permalink";
    public static final String SHOW_LICENSENAME = "licenseName";
    public static final String SHOW_LICENSEURL = "licenseUrl";
    public static final String SHOW_SOURCELINK = "sourceLink";
    public static final String SHOW_IMAGE = "image";
    public static final String SHOW_COMMENT = "comment";
    public static final String SHOW_PUBLISHED = "published";
    public static final String SHOW_VIEWCOUNT = "viewCount";
    public static final String SHOW_WORDCOUNT = "wordCount";
    public static final String SHOW_VERSION = "version";
    public static final String SHOW_CREATEDAT = "createdAt";
    public static final String SHOW_UPDATEDAT = "updatedAt";
    public static final String SHOW_DELETEDAT = "deletedAt";
}
