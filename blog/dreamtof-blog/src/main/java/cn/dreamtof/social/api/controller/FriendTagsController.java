package cn.dreamtof.social.api.controller;


import cn.dreamtof.blog.social.api.request.FriendTagsPageReq;
import cn.dreamtof.blog.social.api.request.FriendTagsCursorReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.core.base.CursorResult;
import lombok.RequiredArgsConstructor;
import cn.dreamtof.social.domain.model.entity.FriendTags;
import cn.dreamtof.social.application.service.FriendTagsAppService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import java.util.UUID;

/**
 * FriendTagsController
 * <p>
 * 友链标签表 控制器
 * 职责：接收 HTTP 请求，执行参数解析与基础校验，调用业务服务层。
 * </p>
 *
 * @author dream
 * @since 
 */
@Tag(name = "社交/友链标签表")
@RestController
@RequestMapping("/social/friendTags")
@RequiredArgsConstructor
public class FriendTagsController {

    private final FriendTagsAppService appService;

    /**
     * 新增 友链标签表
     *
     * @param entity 友链标签表 领域实体对象
     * @return 成功返回 true
     */
    @PostMapping("save")
    public BaseResponse<FriendTags> save(@RequestBody FriendTags entity) {
        return ResultUtils.success(appService.create(entity));
    }

    /**
     * 根据 ID 删除 友链标签表
     *
     * @param id 主键 ID
     * @return 成功返回 true
     */
    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> remove(@PathVariable UUID id) {
        return ResultUtils.success(appService.delete(id));
    }

    /**
     * 批量删除 友链标签表
     *
     * @param ids 主键 ID 集合
     * @return 成功返回 true
     */
    @DeleteMapping("removeByIds")
    public BaseResponse<Boolean> removeByIds(@RequestBody List<UUID> ids) {
        return ResultUtils.success(appService.deleteBatch(ids));
    }

    /**
     * 更新 友链标签表
     *
     * @param entity 包含更新信息的 友链标签表 领域实体
     * @return 成功返回 true
     */
    @PutMapping("update")
    public BaseResponse<FriendTags> update(@RequestBody FriendTags entity) {
        return ResultUtils.success(appService.update(entity));
    }

    /**
     * 获取 友链标签表 详情
     *
     * @param id 主键 ID
     * @return 友链标签表 详情数据 (Entity)
     */
    @GetMapping("detail/{id}")
    public BaseResponse<FriendTags> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    /**
     * 查询 友链标签表 列表
     *
     * @return 友链标签表 全量集合 (Entity)
     */
    @GetMapping("list")
    public BaseResponse<List<FriendTags>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    /**
     * 分页查询 友链标签表
     *
     * @param pageRequest 分页请求（包含页码、页长）
     * @return 分页结果 (Page<Entity>)
     */
    @PostMapping("page")
  public BaseResponse<PageResult<FriendTags>> page(@RequestBody FriendTagsPageReq pageRequest) {
          return ResultUtils.success(appService.page(pageRequest));
    }

    /**
     * 游标查询 (瀑布流)
     *
     * @param cursorReq 游标请求参数
     * @return 游标结果集
     */
    @PostMapping("seek")
    public BaseResponse<CursorResult<FriendTags>> seek(@RequestBody FriendTagsCursorReq cursorReq) {
        return ResultUtils.success(appService.seek(cursorReq));
    }
}