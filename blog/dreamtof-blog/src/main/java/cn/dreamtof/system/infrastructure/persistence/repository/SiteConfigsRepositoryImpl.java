package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.SiteConfigsAssembler;
import cn.dreamtof.system.domain.model.entity.SiteConfigs;
import cn.dreamtof.system.domain.repository.SiteConfigsRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.SiteConfigsMapper;
import cn.dreamtof.system.infrastructure.persistence.po.SiteConfigsPO;
import cn.dreamtof.system.infrastructure.persistence.po.table.SiteConfigsTableDef;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SiteConfigsRepositoryImpl extends ServiceImpl<SiteConfigsMapper, SiteConfigsPO> implements SiteConfigsRepository {

    private final SiteConfigsAssembler assembler;

    private static final SiteConfigsTableDef T = SiteConfigsTableDef.SITE_CONFIGS_PO;

    @Override
    public SiteConfigs create(SiteConfigs entity) {
        SiteConfigsPO po = assembler.toPO(entity);
        if (super.save(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public boolean removeById(UUID id) {
        return super.removeById(id);
    }

    @Override
    public SiteConfigs update(SiteConfigs entity) {
        SiteConfigsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public SiteConfigs getById(UUID id) {
        SiteConfigsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<SiteConfigs> listAll() {
        List<SiteConfigsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<SiteConfigs> page(PageReq pageReq) {
        Page<SiteConfigsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.CONFIG_KEY.asc());
        Page<SiteConfigsPO> resultPage = super.page(flexPage, qw);
        List<SiteConfigs> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<SiteConfigs> entities) {
        List<SiteConfigsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<SiteConfigs> listByIds(List<UUID> ids) {
        List<SiteConfigsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<SiteConfigs> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<SiteConfigsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<SiteConfigsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public SiteConfigs findByKey(String configKey) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.CONFIG_KEY.eq(configKey));
        qw.limit(1);
        SiteConfigsPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }

    @Override
    public List<SiteConfigs> listByKeyPrefix(String keyPrefix) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.CONFIG_KEY.likeRaw(keyPrefix + "%"));
        qw.orderBy(T.CONFIG_KEY.asc());
        List<SiteConfigsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }
}
