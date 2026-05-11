package cn.dreamtof.portfolio.domain.model.entity;

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
@Schema(name = "ProjectTechStacks", description = "项目技术栈关联表 领域实体")
public class ProjectTechStacks implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "关联的项目 ID")
    private UUID projectId;
    @Schema(description = "技术名称")
    private String techName;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "关联创建时间")
    private OffsetDateTime createdAt;

    public static ProjectTechStacks create(UUID projectId, String techName) {
        Asserts.notNull(projectId, "项目ID不能为空");
        Asserts.notBlank(techName, "技术名称不能为空");
        ProjectTechStacks entity = new ProjectTechStacks();
        entity.projectId = projectId;
        entity.techName = techName;
        return entity;
    }
}
