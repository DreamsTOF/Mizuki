package cn.dreamtof.social.api.controller;


import cn.dreamtof.blog.social.api.request.CommentsPageReq;
import cn.dreamtof.blog.social.api.request.CommentsCursorReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.core.base.CursorResult;
import lombok.RequiredArgsConstructor;
import cn.dreamtof.social.domain.model.entity.Comments;
import cn.dreamtof.social.application.service.CommentsAppService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import java.util.UUID;

/**
 * CommentsController
 * <p>
 * 评论表 控制器
 * 职责：接收 HTTP 请求，执行参数解析与基础校验，调用业务服务层。
 * </p>
 *
 * @author dream
 * @since 
 */
@Tag(name = "社交/评论表")
@RestController
@RequestMapping("/social/comments")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsAppService appService;

    /**
     * 新增 评论表
     *
     * @param entity 评论表 领域实体对象
     * @return 成功返回 true
     */
    @PostMapping("save")
    public BaseResponse<Comments> save(@RequestBody Comments entity) {
        return ResultUtils.success(appService.create(entity));
    }

    /**
     * 根据 ID 删除 评论表
     *
     * @param id 主键 ID
     * @return 成功返回 true
     */
    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> remove(@PathVariable UUID id) {
        return ResultUtils.success(appService.delete(id));
    }

    /**
     * 批量删除 评论表
     *
     * @param ids 主键 ID 集合
     * @return 成功返回 true
     */
    @DeleteMapping("removeByIds")
    public BaseResponse<Boolean> removeByIds(@RequestBody List<UUID> ids) {
        return ResultUtils.success(appService.deleteBatch(ids));
    }

    /**
     * 更新 评论表
     *
     * @param entity 包含更新信息的 评论表 领域实体
     * @return 成功返回 true
     */
    @PutMapping("update")
    public BaseResponse<Comments> update(@RequestBody Comments entity) {
        return ResultUtils.success(appService.update(entity));
    }

    /**
     * 获取 评论表 详情
     *
     * @param id 主键 ID
     * @return 评论表 详情数据 (Entity)
     */
    @GetMapping("detail/{id}")
    public BaseResponse<Comments> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    /**
     * 查询 评论表 列表
     *
     * @return 评论表 全量集合 (Entity)
     */
    @GetMapping("list")
    public BaseResponse<List<Comments>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    /**
     * 分页查询 评论表
     *
     * @param pageRequest 分页请求（包含页码、页长）
     * @return 分页结果 (Page<Entity>)
     */
    @PostMapping("page")
  public BaseResponse<PageResult<Comments>> page(@RequestBody CommentsPageReq pageRequest) {
          return ResultUtils.success(appService.page(pageRequest));
    }

    /**
     * 游标查询 (瀑布流)
     *
     * @param cursorReq 游标请求参数
     * @return 游标结果集
     */
    @PostMapping("seek")
    public BaseResponse<CursorResult<Comments>> seek(@RequestBody CommentsCursorReq cursorReq) {
        return ResultUtils.success(appService.seek(cursorReq));
    }
}