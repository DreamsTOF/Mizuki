package cn.dreamtof.content.infrastructure.persistence.po;

import cn.dreamtof.core.base.CreateTimeAudit;
import cn.dreamtof.core.base.UpdateTimeAudit;
import cn.dreamtof.core.base.VersionAudit;

import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;



/**
 * 文章主表 领域实体
 * <p>
 * 职责：核心业务逻辑、领域行为校验、审计数据持有。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name="PostsPO", description = "文章主表 领域实体")
public class PostsPO implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 文章标题
     */
     @Schema(description = "文章标题")
     private String title;
    /**
     * URL 友好标识符
     */
     @Schema(description = "URL 友好标识符")
     private String slug;
    /**
     * Markdown 正文内容
     */
     @Schema(description = "Markdown 正文内容")
     private String content;
    /**
     * 自动生成的摘要
     */
     @Schema(description = "自动生成的摘要")
     private String excerpt;
    /**
     * 文章描述/SEO 摘要
     */
     @Schema(description = "文章描述/SEO 摘要")
     private String description;
    /**
     * 作者名
     */
     @Schema(description = "作者名")
     private String author;
    /**
     * 分类名称
     */
     @Schema(description = "分类名称")
     private String category;
    /**
     * 语言代码
     */
     @Schema(description = "语言代码")
     private String lang;
    /**
     * 是否为草稿状态
     */
     @Schema(description = "是否为草稿状态")
     private Boolean hasDraft;
    /**
     * 是否置顶
     */
     @Schema(description = "是否置顶")
     private Boolean hasPinned;
    /**
     * 置顶优先级
     */
     @Schema(description = "置顶优先级")
     private Integer priority;
    /**
     * 是否加密
     */
     @Schema(description = "是否加密")
     private Boolean hasEncrypted;
    /**
     * 加密密码哈希
     */
     @Schema(description = "加密密码哈希")
     private String passwordHash;
    /**
     * 密码提示信息
     */
     @Schema(description = "密码提示信息")
     private String passwordHint;
    /**
     * 文章别名
     */
     @Schema(description = "文章别名")
     private String alias;
    /**
     * 自定义固定链接
     */
     @Schema(description = "自定义固定链接")
     private String permalink;
    /**
     * 许可证名称
     */
     @Schema(description = "许可证名称")
     private String licenseName;
    /**
     * 许可证链接
     */
     @Schema(description = "许可证链接")
     private String licenseUrl;
    /**
     * 原文链接
     */
     @Schema(description = "原文链接")
     private String sourceLink;
    /**
     * 封面图片路径
     */
     @Schema(description = "封面图片路径")
     private String coverImage;
    /**
     * 是否启用评论
     */
     @Schema(description = "是否启用评论")
     private Boolean hasCommentEnabled;
    /**
     * 发布日期时间
     */
     @Schema(description = "发布日期时间")
     private OffsetDateTime publishedAt;
    /**
     * 浏览次数
     */
     @Schema(description = "浏览次数")
     private Long viewCount;
    /**
     * 字数统计
     */
     @Schema(description = "字数统计")
     private Integer wordCount;
    /**
     * 乐观锁版本号
     */
     @Schema(description = "乐观锁版本号")
     private Integer version;
    /**
     * 创建时间
     */
     @Schema(description = "创建时间")
     private OffsetDateTime createdAt;
    /**
     * 最后更新时间
     */
     @Schema(description = "最后更新时间")
     private OffsetDateTime updatedAt;
    /**
     * 软删除时间戳
     */
     @Schema(description = "软删除时间戳")
     private OffsetDateTime deletedAt;

    // ==========================================
    // 🚀 领域行为 (Domain Logic)
    // ==========================================

    /**
     * 初始化业务逻辑
     */
    public void init() {
        // 在此处编写创建时的默认值或初始校验逻辑
    }

    /**
     * 业务校验：示例（如权限判断）
     */
    public boolean canBeManagedBy(Object userId) {
        // 利用实体已有的审计字段进行逻辑判断
        return true;
    }
}