package cn.dreamtof.content.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name = "Posts", description = "文章主表 领域实体")
public class Posts implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int EXCERPT_MAX_LENGTH = 200;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "文章标题")
    private String title;
    @Schema(description = "URL 友好标识符")
    private String slug;
    @Schema(description = "Markdown 正文内容")
    private String content;
    @Schema(description = "自动生成的摘要")
    private String excerpt;
    @Schema(description = "文章描述/SEO 摘要")
    private String description;
    @Schema(description = "作者名")
    private String author;
    @Schema(description = "分类名称")
    private String category;
    @Schema(description = "语言代码")
    private String lang;
    @Schema(description = "是否为草稿状态")
    private Boolean draft;
    @Schema(description = "是否置顶")
    private Boolean pinned;
    @Schema(description = "置顶优先级")
    private Integer priority;
    @Schema(description = "是否加密")
    private Boolean encrypted;
    @Schema(description = "加密密码哈希")
    private String password;
    @Schema(description = "密码提示信息")
    private String passwordHint;
    @Schema(description = "文章别名")
    private String alias;
    @Schema(description = "自定义固定链接")
    private String permalink;
    @Schema(description = "许可证名称")
    private String licenseName;
    @Schema(description = "许可证链接")
    private String licenseUrl;
    @Schema(description = "原文链接")
    private String sourceLink;
    @Schema(description = "封面图片路径")
    private String image;
    @Schema(description = "是否启用评论")
    private Boolean comment;
    @Schema(description = "发布日期时间")
    private OffsetDateTime published;
    @Schema(description = "浏览次数")
    private Long viewCount;
    @Schema(description = "字数统计")
    private Integer wordCount;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    public static Posts create(String title, String content, String slug,
                               String description, String author, String category,
                               String lang, Boolean draft, String image,
                               Boolean comment, String licenseName,
                               String licenseUrl, String sourceLink, String alias,
                               String permalink, String passwordHint) {
        Asserts.notBlank(title, "文章标题不能为空");
        Asserts.notBlank(slug, "文章slug不能为空");

        Posts entity = new Posts();
        entity.title = title;
        entity.content = content;
        entity.slug = slug;
        entity.description = description;
        entity.author = author;
        entity.category = category;
        entity.lang = lang != null ? lang : "zh";
        entity.draft = draft != null ? draft : true;
        entity.pinned = false;
        entity.priority = 0;
        entity.encrypted = false;
        entity.password = null;
        entity.passwordHint = passwordHint;
        entity.alias = alias;
        entity.permalink = permalink;
        entity.licenseName = licenseName;
        entity.licenseUrl = licenseUrl;
        entity.sourceLink = sourceLink;
        entity.image = image;
        entity.comment = comment != null ? comment : true;
        entity.viewCount = 0L;
        entity.excerpt = generateExcerpt(content);
        entity.wordCount = 0;
        if (!entity.draft) {
            entity.published = DateUtils.offsetNow();
        }
        return entity;
    }

    public void update(String title, String content, String slug,
                       String description, String author, String category,
                       String lang, String image, Boolean comment,
                       String licenseName, String licenseUrl, String sourceLink,
                       String alias, String permalink, String passwordHint) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
            this.excerpt = generateExcerpt(content);
        }
        if (slug != null) {
            this.slug = slug;
        }
        if (description != null) {
            this.description = description;
        }
        if (author != null) {
            this.author = author;
        }
        if (category != null) {
            this.category = category;
        }
        if (lang != null) {
            this.lang = lang;
        }
        if (image != null) {
            this.image = image;
        }
        if (comment != null) {
            this.comment = comment;
        }
        if (licenseName != null) {
            this.licenseName = licenseName;
        }
        if (licenseUrl != null) {
            this.licenseUrl = licenseUrl;
        }
        if (sourceLink != null) {
            this.sourceLink = sourceLink;
        }
        if (alias != null) {
            this.alias = alias;
        }
        if (permalink != null) {
            this.permalink = permalink;
        }
        if (passwordHint != null) {
            this.passwordHint = passwordHint;
        }
    }

    public void publish() {
        Asserts.isTrue(this.draft, "文章已发布，无需重复发布");
        this.draft = false;
        this.published = DateUtils.offsetNow();
    }

    public void unpublish() {
        Asserts.isFalse(this.draft, "文章已是草稿状态");
        this.draft = true;
        this.published = null;
    }

    public void togglePin(boolean pinned, Integer priority) {
        this.pinned = pinned;
        this.priority = pinned ? (priority != null ? priority : 0) : 0;
    }

    public void encrypt(String password) {
        Asserts.notBlank(password, "密码哈希不能为空");
        this.encrypted = true;
        this.password = password;
    }

    public void decrypt() {
        this.encrypted = false;
        this.password = null;
        this.passwordHint = null;
    }

    public void incrementViewCount() {
        this.viewCount = this.viewCount != null ? this.viewCount + 1 : 1;
    }

    public void updateWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public void markDeleted() {
        Asserts.isTrue(Asserts.isNull(this.deletedAt), "文章已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isPublished() {
        return !Boolean.TRUE.equals(this.draft);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    private static String generateExcerpt(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String plain = content.replaceAll("[#*`>\\-\\[\\]()!|]", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.length() <= EXCERPT_MAX_LENGTH) {
            return plain;
        }
        return plain.substring(0, EXCERPT_MAX_LENGTH) + "...";
    }
}
