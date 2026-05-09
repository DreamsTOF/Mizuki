package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 搜索记录表 游标分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SearchLogsCursorReq", description = "搜索记录表游标分页请求")
public class SearchLogsCursorReq extends CursorReq {
    // 游标字段通常已在基类，此处可扩展其他查询参数
}