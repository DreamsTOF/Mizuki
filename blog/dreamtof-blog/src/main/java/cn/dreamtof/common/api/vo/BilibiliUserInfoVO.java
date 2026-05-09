package cn.dreamtof.common.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BilibiliUserInfoVO", description = "哔哩哔哩用户信息响应对象")
public class BilibiliUserInfoVO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "个人空间链接")
    private String personalSpaceUrl;

    @Schema(description = "用户头像地址")
    private String avatar;

    @Schema(description = "直播间链接")
    private String liveRoomUrl;
}
