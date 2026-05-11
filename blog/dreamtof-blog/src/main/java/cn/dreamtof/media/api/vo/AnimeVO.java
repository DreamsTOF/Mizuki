package cn.dreamtof.media.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AnimeVO", description = "番剧响应对象")
public class AnimeVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "番剧标题")
    private String title;

    @Schema(description = "观看状态")
    private String status;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "封面图片")
    private String cover;

    @Schema(description = "番剧描述")
    private String description;

    @Schema(description = "集数信息")
    private String episodes;

    @Schema(description = "年份")
    private String year;

    @Schema(description = "类型/流派")
    private List<String> genre;

    @Schema(description = "制作公司")
    private String studio;

    @Schema(description = "番剧链接")
    private String link;

    @Schema(description = "当前观看进度")
    private Integer progress;

    @Schema(description = "总集数")
    private Integer totalEpisodes;

    @Schema(description = "开始观看日期")
    private String startDate;

    @Schema(description = "结束观看日期")
    private String endDate;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
