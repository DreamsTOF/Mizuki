package cn.dreamtof.system.api.vo;

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
@Schema(name = "NavLinkVO", description = "导航链接响应对象")
public class NavLinkVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "链接名称")
    private String name;
    @Schema(description = "链接地址")
    private String url;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "是否外部链接")
    private Boolean hasExternal;
    @Schema(description = "是否新窗口打开")
    private Boolean hasNewWindow;
    @Schema(description = "父级链接 ID")
    private UUID parentId;
    @Schema(description = "导航位置")
    private String position;
    @Schema(description = "排序权重")
    private Integer sortOrder;
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    @Schema(description = "子链接列表")
    private List<NavLinkVO> children;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
