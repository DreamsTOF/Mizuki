package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 站点配置表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SiteConfigsPageReq", description = "站点配置表分页查询请求")
public class SiteConfigsPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}