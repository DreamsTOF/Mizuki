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
@Schema(name = "Archives", description = "文章归档索引表 领域实体")
public class Archives implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "归档年份")
    private Integer year;
    @Schema(description = "归档月份")
    private Integer month;
    @Schema(description = "该年月文章数量")
    private Integer postCount;
    @Schema(description = "该年月文章 ID 列表")
    private String postIds;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static Archives create(Integer year, Integer month) {
        Asserts.notNull(year, "归档年份不能为空");
        Asserts.notNull(month, "归档月份不能为空");
        Asserts.isTrue(month >= 1 && month <= 12, "归档月份必须在1-12之间");

        Archives entity = new Archives();
        entity.year = year;
        entity.month = month;
        entity.postCount = 0;
        entity.postIds = "[]";
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void addPost(UUID postId) {
        Asserts.notNull(postId, "文章ID不能为空");
        this.postCount = this.postCount != null ? this.postCount + 1 : 1;
    }

    public void removePost(UUID postId) {
        Asserts.notNull(postId, "文章ID不能为空");
        if (this.postCount != null && this.postCount > 0) {
            this.postCount = this.postCount - 1;
        }
    }
}
