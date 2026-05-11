package cn.dreamtof.portfolio.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SkillVO", description = "技能响应对象")
public class SkillVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "技能名称")
    private String name;

    @Schema(description = "技能描述")
    private String description;

    @Schema(description = "Iconify图标名称")
    private String icon;

    @Schema(description = "技能分类")
    private String category;

    @Schema(description = "技能等级")
    private String level;

    @Schema(description = "经验时长")
    private Experience experience;

    @Schema(description = "关联项目ID列表")
    private List<String> projects;

    @Schema(description = "认证证书列表")
    private List<String> certifications;

    @Schema(description = "主题色")
    private String color;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Experience", description = "经验时长")
    public static class Experience {
        @Schema(description = "年数")
        private int years;
        @Schema(description = "月数")
        private int months;
    }
}
