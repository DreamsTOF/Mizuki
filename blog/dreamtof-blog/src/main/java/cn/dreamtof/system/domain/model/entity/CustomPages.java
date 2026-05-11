package cn.dreamtof.system.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
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
@Schema(name = "CustomPages", description = "自定义页面表 领域实体")
public class CustomPages implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "页面唯一标识")
    private String pageKey;

    @Schema(description = "页面标题")
    private String title;

    @Schema(description = "页面内容")
    private String content;

    @Schema(description = "页面描述")
    private String description;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "是否允许评论")
    private Boolean hasCommentEnabled;

    @Schema(description = "是否启用")
    private Boolean hasEnabled;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    public static CustomPages create(String pageKey, String title, String content,
                                     String description, String coverImage,
                                     Boolean hasCommentEnabled, Boolean hasEnabled) {
        Asserts.notBlank(pageKey, "页面唯一标识不能为空");
        Asserts.notBlank(title, "页面标题不能为空");
        CustomPages entity = new CustomPages();
        entity.pageKey = pageKey;
        entity.title = title;
        entity.content = content;
        entity.description = description;
        entity.coverImage = coverImage;
        entity.hasCommentEnabled = hasCommentEnabled != null ? hasCommentEnabled : false;
        entity.hasEnabled = hasEnabled != null ? hasEnabled : true;
        return entity;
    }

    public void updateContent(String title, String content, String description,
                              String coverImage, Boolean hasCommentEnabled, Boolean hasEnabled) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (description != null) {
            this.description = description;
        }
        if (coverImage != null) {
            this.coverImage = coverImage;
        }
        if (hasCommentEnabled != null) {
            this.hasCommentEnabled = hasCommentEnabled;
        }
        if (hasEnabled != null) {
            this.hasEnabled = hasEnabled;
        }
    }

    public void enable() {
        this.hasEnabled = true;
    }

    public void disable() {
        this.hasEnabled = false;
    }
}
