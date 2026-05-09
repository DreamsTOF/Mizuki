package cn.dreamtof.system.infrastructure.persistence.repository;


import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import cn.dreamtof.blog.system.infrastructure.persistence.po.BannersPO; // PO 类
import cn.dreamtof.blog.system.domain.model.entity.Banners;   // 领域层 Entity
import cn.dreamtof.blog.system.infrastructure.persistence.mapper.BannersMapper;
import cn.dreamtof.blog.system.domain.repository.BannersRepository;
import cn.dreamtof.blog.system.application.assembler.BannersAssembler; // Assembler 移至此处
import cn.dreamtof.blog.system.api.request.BannersPageReq;
import cn.dreamtof.blog.system.api.request.BannersCursorReq;
import cn.dreamtof.core.base.CursorResult;
import org.springframework.stereotype.Repository;
import com.mybatisflex.core.paginate.Page;
import cn.dreamtof.core.base.PageResult;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 横幅图片表 仓储实现 (Infrastructure Layer)
 * <p>
 * 职责：实现领域层定义的仓储接口，直接与数据库交互。
 * 采用显式 super 调用，确保直接触发底层框架行为，消除递归隐患。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BannersRepositoryImpl extends ServiceImpl<BannersMapper, BannersPO> implements BannersRepository {

    private final BannersAssembler assembler; // 注入 MapStruct 转换器

    // ================== 1. 基础透传操作 (使用 super) ==================

   @Override
   public Banners create(Banners entity) {
       // Entity -> PO
       BannersPO po = assembler.toPO(entity);
       // MyBatis-Flex 的 super.save(po) 返回 boolean
       if (super.save(po)) {
           // 执行成功后，Flex 会自动回填主键 ID 到 po 对象中
           return assembler.toEntity(po);
       }
       return null;
   }

    @Override
    public boolean removeById(UUID id) {
        return super.removeById(id);
    }

    @Override
    public Banners update(Banners entity) {
        // Entity -> PO
        BannersPO po = assembler.toPO(entity);
        // MyBatis-Flex 的 super.updateById(po) 返回 boolean
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Banners getById(UUID id) {
        BannersPO po = super.getById(id);
        // PO -> Entity
        return assembler.toEntity(po);
    }

    @Override
    public List<Banners> listAll() {
        // 查出 PO 列表
        List<BannersPO> poList = super.list();
        // PO List -> Entity List
        return assembler.toEntityList(poList);
    }

  /**
   * 分页查询实现
   */
  @Override
  public PageResult<Banners> page(BannersPageReq pageReq) {
      // 1. 初始化 MyBatis-Flex 的 Page 对象
      Page<BannersPO> flexPage = Page.of(
          pageReq.getPageNum(),
          pageReq.getPageSize()
      );
      // 2. 构造查询条件对象 (QueryWrapper)
      // 此时可根据业务需求，将 pageReq 中的过滤字段映射为数据库查询条件
      QueryWrapper queryWrapper = QueryWrapper.create();

      // 此处可以根据需求扩展过滤条件，例如：
      // if (pageReq.getSomeField() != null) {
      //     queryWrapper.where(TABLE_NAME_PO.SOME_FIELD.eq(pageReq.getSomeField()));
      // }

      // 3. 执行持久层分页查询
      Page<BannersPO> resultPage = super.page(flexPage, queryWrapper);

      // 4. 数据层级转换 (PO -> Entity)
      List<Banners> entityList = assembler.toEntityList(resultPage.getRecords());

      // 5. 组装并返回通用的 PageResult 对象
      return PageResult.of(
          entityList,
          resultPage.getTotalRow(),
          resultPage.getTotalPage(),
          resultPage.getPageNumber(),
          resultPage.getPageSize()
      );
  }

    // ================== 2. 增强逻辑实现 ==================

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        // 调用 ServiceImpl 内置的批量按 ID 删除
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Banners> entities) {
        // Entity List -> PO List
        List<BannersPO> pos = assembler.toPOList(entities);
        // 调用 ServiceImpl 内置的批量保存，默认 1000 条一提交
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        // 利用 Flex 的 queryChain 快速判断，不涉及对象实例化，性能极佳
        return queryChain().where(BannersPO::getId).eq(id).exists();
    }

    @Override
    public List<Banners> listByIds(List<UUID> ids) {
        List<BannersPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Banners> seek(BannersCursorReq req) {
        // 1. 类型安全转换：将 Serializable 游标转为具体的 UUID
        UUID lastId = null;
        if (req.getCursor() != null) {
            lastId = (UUID) req.getCursor();
        }

        // 2. 执行持久层查询 (多查 1 条用于判断 hasNext)
        List<BannersPO> poList = queryChain()
                // 使用 gt (大于) 实现游标跳转，主键必须有序
                .where(BannersPO::getId).gt(lastId)
                .orderBy(BannersPO::getId).asc()
                .limit(req.getLimit() + 1)
                .list();

        // 3. 判断是否有下一页并截取数据
        boolean hasNext = poList.size() > req.getLimit();
        List<BannersPO> resultList = hasNext ? poList.subList(0, req.getLimit()) : poList;

        // 4. 计算下一个游标值
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }

        // 5. 转换并返回
        return new CursorResult<>(
            assembler.toEntityList(resultList),
            nextCursor,
            hasNext
        );
    }
}