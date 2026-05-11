package cn.dreamtof.content.domain.model.entity;

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
@Schema(name = "PostTags", description = "文章-标签关联表 领域实体")
public class PostTags implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "关联的文章 ID")
    private UUID postId;
    @Schema(description = "关联的标签 ID")
    private UUID tagId;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "关联创建时间")
    private OffsetDateTime createdAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static PostTags create(UUID postId, UUID tagId) {
        Asserts.notNull(postId, "文章ID不能为空");
        Asserts.notNull(tagId, "标签ID不能为空");

        PostTags entity = new PostTags();
        entity.postId = postId;
        entity.tagId = tagId;
        return entity;
    }
}
