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
@Schema(name = "NavLinkTreeVO", description = "导航链接树响应对象")
public class NavLinkTreeVO {

    @Schema(description = "导航链接列表（树形）")
    private List<NavLinkVO> links;
}
