package cn.dreamtof.content.api.vo;

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
@Schema(name = "DiaryVO", description = "日记响应对象")
public class DiaryVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "日记正文内容")
    private String content;

    @Schema(description = "日记日期")
    private String date;

    @Schema(description = "图片URL列表")
    private List<String> images;

    @Schema(description = "地点信息")
    private String location;

    @Schema(description = "心情描述")
    private String mood;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
