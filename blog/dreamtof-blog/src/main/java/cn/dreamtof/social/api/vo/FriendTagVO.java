package cn.dreamtof.social.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "FriendTagVO", description = "友链标签响应对象")
public class FriendTagVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "标签颜色")
    private String color;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
