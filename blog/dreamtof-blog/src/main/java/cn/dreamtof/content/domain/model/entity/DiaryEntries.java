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
@Schema(name = "DiaryEntries", description = "日记条目表 领域实体")
public class DiaryEntries implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "日记正文内容")
    private String content;
    @Schema(description = "日记日期时间")
    private OffsetDateTime date;
    @Schema(description = "图片 URL 数组")
    private String images;
    @Schema(description = "地点信息")
    private String location;
    @Schema(description = "心情描述")
    private String mood;
    @Schema(description = "标签数组")
    private String tags;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static DiaryEntries create(String content, OffsetDateTime date,
                                      String images, String location,
                                      String mood, String tags) {
        Asserts.notBlank(content, "日记内容不能为空");

        DiaryEntries entity = new DiaryEntries();
        entity.content = content;
        entity.date = date != null ? date : DateUtils.offsetNow();
        entity.images = images;
        entity.location = location;
        entity.mood = mood;
        entity.tags = tags;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String content, OffsetDateTime date,
                       String images, String location,
                       String mood, String tags) {
        if (content != null) {
            this.content = content;
        }
        if (date != null) {
            this.date = date;
        }
        if (images != null) {
            this.images = images;
        }
        if (location != null) {
            this.location = location;
        }
        if (mood != null) {
            this.mood = mood;
        }
        if (tags != null) {
            this.tags = tags;
        }
    }

    public void markDeleted() {
        Asserts.isTrue(Asserts.isNull(this.deletedAt), "日记已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }
}
