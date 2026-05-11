package cn.dreamtof.content.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ArchiveVO", description = "归档响应对象")
public class ArchiveVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "归档年份")
    private Integer year;
    @Schema(description = "归档月份")
    private Integer month;
    @Schema(description = "该年月文章数量")
    private Integer postCount;
}
