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
@Schema(name = "Tags", description = "标签表 领域实体")
public class Tags implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "URL 友好的标签标识")
    private String slug;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static Tags create(String name, String slug) {
        Asserts.notBlank(name, "标签名称不能为空");
        Asserts.notBlank(slug, "标签slug不能为空");

        Tags entity = new Tags();
        entity.name = name;
        entity.slug = slug;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String name, String slug) {
        if (name != null) {
            this.name = name;
        }
        if (slug != null) {
            this.slug = slug;
        }
    }
}
