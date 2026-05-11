package cn.dreamtof.social.api.vo;

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
@Schema(name = "CommentVO", description = "评论响应对象")
public class CommentVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "评论目标ID")
    private UUID targetId;
    @Schema(description = "评论目标类型")
    private String targetType;
    @Schema(description = "评论者昵称")
    private String nickname;
    @Schema(description = "评论者邮箱")
    private String email;
    @Schema(description = "评论者网站")
    private String website;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "父评论ID")
    private UUID parentId;
    @Schema(description = "是否已审核")
    private Boolean hasApproved;
    @Schema(description = "点赞数")
    private Integer likeCount;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
