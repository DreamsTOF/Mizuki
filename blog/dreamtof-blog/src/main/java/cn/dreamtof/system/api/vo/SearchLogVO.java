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
@Schema(name = "SearchLogVO", description = "搜索记录响应对象")
public class SearchLogVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "搜索关键词")
    private String keyword;
    @Schema(description = "搜索结果数量")
    private Integer resultCount;
    @Schema(description = "搜索时间")
    private OffsetDateTime searchedAt;
}
