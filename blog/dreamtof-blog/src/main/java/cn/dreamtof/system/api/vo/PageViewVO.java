package cn.dreamtof.system.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PageViewVO", description = "页面访问响应对象")
public class PageViewVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "访问页面路径")
    private String pagePath;
    @Schema(description = "页面类型")
    private String pageType;
    @Schema(description = "关联目标ID")
    private UUID targetId;
    @Schema(description = "访问者IP")
    private String ipAddress;
    @Schema(description = "来源页面")
    private String referer;
    @Schema(description = "访问时间")
    private OffsetDateTime visitedAt;
}
