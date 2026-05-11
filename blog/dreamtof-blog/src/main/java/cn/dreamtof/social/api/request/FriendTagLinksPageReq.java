package cn.dreamtof.social.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 友链-标签关联表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FriendTagLinksPageReq", description = "友链-标签关联表分页查询请求")
public class FriendTagLinksPageReq extends PageReq {
    // 可以在此根据 table.columns 生成特定的过滤字段，如 name, status 等
}