package cn.dreamtof.content.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TagVO", description = "标签响应对象")
public class TagVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "URL 友好的标签标识")
    private String slug;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
