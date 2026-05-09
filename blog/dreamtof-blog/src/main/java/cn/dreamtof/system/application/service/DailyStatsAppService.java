package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 每日统计汇总表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DailyStatsPageReq", description = "每日统计汇总表分页查询请求")
public class DailyStatsPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}