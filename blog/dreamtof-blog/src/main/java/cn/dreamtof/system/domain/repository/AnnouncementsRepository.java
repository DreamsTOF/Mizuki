package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 公告表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AnnouncementsPageReq", description = "公告表分页查询请求")
public class AnnouncementsPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}