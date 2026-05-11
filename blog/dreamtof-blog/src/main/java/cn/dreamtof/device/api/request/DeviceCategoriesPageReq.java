package cn.dreamtof.device.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 设备分类表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DeviceCategoriesPageReq", description = "设备分类表分页查询请求")
public class DeviceCategoriesPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}